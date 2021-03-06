package log4JToXml.propertiesToXml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by FH on 7.5.2015.
 */
public class XmlPropertiesBuilder {

    private Document doc;
    private Properties config;
    private List<Package> all;
	
	private static final String XMLNS_Log4j = "http://jakarta.apache.org/log4j/";
	private static final String DOCTYPE_STRING = "../log4j.dtd";
	
    private static final List<String> OUTPUT_LEVELS = new ArrayList<>(Arrays.asList("ALL", "OFF", "TRACE", "DEBUG", "WARN", "INFO", "FATAL", "ERROR"));
	private static final List<String> validLvl1 = new ArrayList<>(Arrays.asList("appender","logger","category","additivity","threshold","debug","rootCategory","rootLogger", "renderer", "level"));

    /**
     * Upon instantiation, builds an xml file which is a Log4J configuration equivalent to the input .properties file. Remember to call save()
     *
     * @param config the .properties file containing the Log4J configuration
     */
    public XmlPropertiesBuilder(Properties config) 
	{
		if (config == null) {
			throw new IllegalArgumentException("The config parameter cannot be null.");
		}
		if (config.keySet().isEmpty()) {
			throw new IllegalArgumentException("The supplied properties file is empty");
		}
        this.config = config;
        createEmptyDocument();
        Element root = doc.createElement("log4j:configuration");
        root.setAttribute("xmlns:log4j", XMLNS_Log4j);
        doc.appendChild(root);

        List<Package> all = new ArrayList<>();
        for (Object s : config.keySet()) {
            String st = (String) s;
			if(!st.startsWith("log4j"))
			{
				throw new IllegalArgumentException("The file seems to be invalid. Every property should start with log4j.");
			}
			if(config.getProperty(st).trim().equals(""))
			{
				throw new IllegalArgumentException("The properties file is invalid. A property only has whitespace for a value.");
			}
            Package aPackage = new Package(config.getProperty(st), Arrays.asList(st.split("\\.")));
			if(!validLvl1.contains(aPackage.getLevel(1)))
			{
				throw new IllegalArgumentException("The properties file is invalid. Unrecognized property "+aPackage.getLevel(1)+" in "+aPackage.groupLevelsFrom(0));
			}
            all.add(aPackage);
        }
        this.all = all;
		
        Set<String> appenderNames = all.stream()
                .filter(o -> o.getLevel(1).equals("appender"))
                .map(o -> o.getLevel(2))
                .collect(Collectors.toSet());

        handleAppenders(appenderNames);
        handleRootLogger();
        handleLoggers();
        handleCategories();
		handleRenderers();
    }

    private void handleLoggers() {
        Set<Package> loggerLines = all.stream()
                .filter(o -> o.getLevel(1).equals("logger"))
                .collect(Collectors.toSet());
        handleLoggersOrCategories(loggerLines);
    }

    private void handleCategories() {
        //log4j.category.DataNucleus.Utility=WARN, A1
        //this behaves exactly the same way as a logger
        Set<Package> categoryLines = all.stream()
                .filter(o -> o.getLevel(1).equals("category"))
                .collect(Collectors.toSet());
        handleLoggersOrCategories(categoryLines);
    }

    private void handleLoggersOrCategories(Set<Package> lines) {
        //log4j.logger or log4j.category?
        try {
            boolean areWeDoingLoggersNow = lines.stream().findAny().get().getLevel(1).equals("logger");

            //each logger has an additivity attribute, specified separately (defaults to true)
            Set<Package> additivityLines = all.stream()
                    .filter(o -> o.getLevel(1).equals("additivity"))
                    //anything else would mean the document is invalid, but let's just skip the lines
                    .filter(o -> o.getValue().equals("false") || o.getValue().equals("true"))
                    .collect(Collectors.toSet());

            for (Package line : lines) {
                String className = line.groupLevelsFrom(2);

                Element elem = areWeDoingLoggersNow ? doc.createElement("logger") : doc.createElement("category");
                elem.setAttribute("name", className);
                Optional<Package> additivityParameter = additivityLines.stream()
                        .filter(o -> o.groupLevelsFrom(2).equals(className))
                        .findFirst();

                if (additivityParameter.isPresent() && additivityParameter.get().getValue().equals("false")) {
                    elem.setAttribute("additivity", "false");
                }

                String[] split = line.getValue().split(",");
                addLevelAttribute(elem, split[0]);
                for (int i = 1; i < split.length; i++) {
                    Element appenderRefElement = doc.createElement("appender-ref");
                    appenderRefElement.setAttribute("ref", split[i].trim());
                    elem.appendChild(appenderRefElement);
                }

                doc.getDocumentElement().appendChild(elem);
            }
        } catch (NoSuchElementException ex) {
            //the set is empty, we're not doing anything at all
            return;
        }
    }

    private void handleRootLogger() {
        //log4j.rootLogger=LEVEL, appender-ref
        try {
            Package line = all.stream()
                    .filter(o -> o.getLevel(1).equals("rootLogger")).findFirst().get();

            String[] rootChildren = line.getValue().split(",");
            Element rootElem = doc.createElement("root");
            Element levelElem = doc.createElement("level");
            addLevelAttribute(rootElem, rootChildren[0]);

            for (int i = 1; i < rootChildren.length; i++) {
                Element appenderRefElement = doc.createElement("appender-ref");
                appenderRefElement.setAttribute("ref", rootChildren[i].trim());
                rootElem.appendChild(appenderRefElement);
            }

            doc.getDocumentElement().appendChild(rootElem);
        } catch (NoSuchElementException ex) {
            //this is fine.
            System.out.println("No root logger present in the configuration.");
        }
    }

    private void addLevelAttribute(Element parentElem, String levelValue) {
        if (!OUTPUT_LEVELS.contains(levelValue.toUpperCase())) {
            throw new IllegalStateException("Unrecognized output level.");
        }
        Element levelElem = doc.createElement("level");
        levelElem.setAttribute("value", levelValue.toUpperCase());
        parentElem.appendChild(levelElem);
    }

    private void handleAppenders(Set<String> appenderNames) {
        for (String appenderName : appenderNames) {
            //log4j.appender.NAME
            List<Package> relevantProperties = all.stream()
                    .filter(o -> o.numLevels() > 2)
                    .filter(o -> appenderName.equals(o.getLevel(2)))
                    .collect(Collectors.toList());

            AppenderParser appenderParser = new AppenderParser();
            appenderParser.parse(relevantProperties, doc);
        }
    }
	
	
	private void handleRenderers() {
		List<Package> relevantLines = all.stream()
				.filter(o->o.getLevel(1).equals("renderer"))
				.collect(Collectors.toList());
		
		for(Package line : relevantLines)
		{
			Element rendererElement = doc.createElement("renderer");
			rendererElement.setAttribute("renderedClass", line.groupLevelsFrom(2));
			rendererElement.setAttribute("renderingClass", line.getValue());
			doc.appendChild(rendererElement);
		}
	}

    /**
     * Saves the converted XML document
     *
     * @param path where to save the file
     * @throws IOException when the saving goes wrong
     */
    public void saveXmlDocument(String path) throws IOException {
		if(path==null)
		{
			throw new IllegalArgumentException("The path to the file cannot be null.");
		}
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, DOCTYPE_STRING);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        DOMSource source = new DOMSource(doc);
        StreamResult result = null;
        try {
            try ( //create new temporary file
                    PrintWriter temp = new PrintWriter(path, "UTF-8")) {
                File tempXML = new File(path);
                result = new StreamResult(new File(path).getPath());
            }
        } catch (IOException ex) {
            throw ex;
        }

        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            System.out.println(ex.getStackTrace());
        }

    }

    private void createEmptyDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            this.doc = builder.newDocument();
        } catch (ParserConfigurationException e) {
            System.out.println("Could not create xml document.");
            e.printStackTrace();
        }
    }
}
