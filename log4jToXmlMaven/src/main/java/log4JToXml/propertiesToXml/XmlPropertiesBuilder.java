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
    /**
     * Outputs an xml file which is a Log4J configuration equivalent to the input .properties file
     *
     * @param config the .properties file containing the Log4J configuration
     */
    public static List<String> OUTPUT_LEVELS = new ArrayList<>(Arrays.asList("ALL", "OFF", "TRACE", "TRACE_INT", "DEBUG", "WARN", "INFO", "FATAL", "ERROR"));

    public XmlPropertiesBuilder(Properties config) {
        this.config = config;
        createEmptyDocument();
        Element root = doc.createElement("log4j:configuration");
        root.setAttribute("xmlns:log4j", "http://jakarta.apache.org/log4j/");
        doc.appendChild(root);

        List<Package> all = new ArrayList<>();
        for (Object s : config.keySet()) {
            String st = (String) s;
            Package aPackage = new Package(config.getProperty(st), Arrays.asList(st.split("\\.")));
            all.add(aPackage);
        }
        this.all = all;

        //look for the logger names in the rootLogger property
        String rootLogger = config.getProperty("log4j.rootLogger");
        String[] split = rootLogger.split(",");
        //the first value is a level, the rest are logger names
        String level = split[0];
        Set<String> appenderNames = all.stream()
                .filter(o -> o.getLevel(1).equals("appender"))
                .map(o -> o.getLevel(2))
                .collect(Collectors.toSet());

        handleAppenders(appenderNames);
        handleRootLogger();
        handleLoggers();
        handleCategories();
        //saveXmlDocument();
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
                System.out.println(className);

                Element elem = areWeDoingLoggersNow ? doc.createElement("logger") : doc.createElement("category");
                elem.setAttribute("name", className);
                Optional<Package> additivityParameter = additivityLines.stream()
                        .filter(o -> o.groupLevelsFrom(2).equals("className"))
                        .findFirst();

                elem.setAttribute("additivity", additivityParameter.isPresent() ? additivityParameter.get().getValue() : "true");

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
        Element rootElem = doc.createElement("root");

        Package line = all.stream()
                .filter(o -> o.getLevel(1).equals("rootLogger")).findFirst().get();

        String[] rootChildren = line.getValue().split(",");

        Element levelElem = doc.createElement("level");
        addLevelAttribute(rootElem, rootChildren[0]);

        for (int i = 1; i < rootChildren.length; i++) {
            Element appenderRefElement = doc.createElement("appender-ref");
            appenderRefElement.setAttribute("ref", rootChildren[i].trim());
            rootElem.appendChild(appenderRefElement);
        }

        doc.getDocumentElement().appendChild(rootElem);
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
            appenderParser.parse(relevantProperties, all, doc);
        }
    }

    public void saveXmlDocument(String path) throws IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "./src/main/resources/log4j.dtd");
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        DOMSource source = new DOMSource(doc);
        StreamResult result = null;
        try {
            try ( //create new temporary file
                    PrintWriter temp = new PrintWriter(path, "UTF-8")) {
                File tempXML = new File(path);
//                result = new StreamResult(tempXML);
                result = new StreamResult(new File(path).getPath());
            }
        } catch (IOException ex) {
            throw ex;
        }

        
        //StreamResult result = new StreamResult(System.out);
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