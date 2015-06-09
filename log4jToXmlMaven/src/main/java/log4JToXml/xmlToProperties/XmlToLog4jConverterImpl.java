/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package log4JToXml.xmlToProperties;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Converter log4j.xml to log4j.properties implementation
 *
 * @author xstefank
 * @version 1.0
 */
public class XmlToLog4jConverterImpl implements XmlToLog4jConverter {

    private static final Logger log = Logger.getLogger(XmlToLog4jConverterImpl.class);

    private File tempXML;
    private File tempDTD;
    private PrintStream documentStream;
    private Document doc;
    private Properties log4jProperties;

    /**
     * Constructor for basic converter, default settings
     *
     * @throws IOException if there is problem creating temporary file in project location
     */
    public XmlToLog4jConverterImpl() throws IOException {

        //needed for the order of the properties
        log4jProperties = new LinkedProperties();
        doc = null;

        tempXML = File.createTempFile("tempXML", ".xml");
        documentStream = new PrintStream(tempXML);

        InputStream initialStream = this.getClass().getResourceAsStream("/log4j.dtd");

        tempDTD = File.createTempFile("tempDTD", ".dtd");
        FileUtils.copyInputStreamToFile(initialStream, tempDTD);

    }

    @Override
    public void load(String filename) {
        URL url = null;

        //add DTD declaration, fill temp file
        try {
            addDTDDeclaration(filename);
        } catch (XMLStreamException ex) {
            log.error("Error opening input file: " + filename, ex);
            throw new IllegalArgumentException("Invalid input: " + filename, ex);
        }

        try {
            url = tempXML.toURI().toURL();
        } catch (MalformedURLException ex) {
            log.error("Error transforming given filename to URL: " + filename, ex);
            throw new IllegalArgumentException("Invalid file name", ex);
        }

        try {
            doc = parse(url);
        } catch (DocumentException ex) {
            log.error("Error parsing input file: " + filename, ex);
            throw new IllegalArgumentException("Invalid input file content; Error parsing input file", ex);
        }
    }

    @Override
    public void convert() {

        if (doc == null) {
            throw new IllegalStateException("There is no loaded input to be converted");
        }

        //root node parsing, root element is optional
        Node rootNode = doc.selectSingleNode("/log4j:configuration/root");

        if (rootNode != null) {

            String rootContent = "";

            Node priority = rootNode.selectSingleNode("./priority | ./level");
            rootContent += priority.valueOf("@value").toUpperCase();

            //appenders refernces
            List<Node> appendersRefs = rootNode.selectNodes("./appender-ref");
            for (Node appender : appendersRefs) {
                rootContent += ", " + appender.valueOf("@ref");
            }

            String rootName = "log4j.rootLogger";
            log4jProperties.setProperty(rootName, rootContent);

            //root params
            List<Node> rootParams = rootNode.selectNodes("./param");

            if (!rootParams.isEmpty()) {
                for (Node param : rootParams) {
                    log4jProperties.setProperty(rootName + "." + param.valueOf("@name"),
                            param.valueOf("@value"));
                }
            }

        }

        //renderer nodes, optional
        List<Node> rendererNodes = doc.selectNodes("/log4j:configuration/renderer");

        if (!rendererNodes.isEmpty()) {
            for (Node renderer : rendererNodes) {
                log4jProperties.setProperty("log4j.renderer." + renderer.valueOf("@renderedClass"),
                        renderer.valueOf("@renderingClass"));
            }
        }

        //appender nodes, optional
        List<Node> appenderNodes = doc.selectNodes("/log4j:configuration/appender");

        if (!appenderNodes.isEmpty()) {

            String appenderName = null;

            for (Node appender : appenderNodes) {
                appenderName = "log4j.appender." + appender.valueOf("@name");

                log4jProperties.setProperty(appenderName, appender.valueOf("@class"));

                //errorHandler, optional 0-1
                Node errorHandler = appender.selectSingleNode("./errorHandler");

                if (errorHandler != null) {

                    String errorHandlerName = appenderName + ".errorhandler";

                    log4jProperties.setProperty(errorHandlerName, errorHandler.valueOf("@class"));

                    //EH params, optional
                    List<Node> eHParams = errorHandler.selectNodes("./param");

                    if (!eHParams.isEmpty()) {
                        for (Node param : eHParams) {
                            log4jProperties.setProperty(errorHandlerName + "." + param.valueOf("@name"), param.valueOf("@value"));
                        }
                    }

                    //EH root-ref, 0-1
                    Node rootRef = errorHandler.selectSingleNode("./root-ref");

                    if (rootRef != null) {
                        log4jProperties.setProperty(errorHandlerName + ".root-ref", "true");
                    }

                    //EH logger-ref, optional
                    List<Node> eHLoggerRefs = errorHandler.selectNodes("./logger-ref");

                    if (!eHLoggerRefs.isEmpty()) {
                        String loggersRefValue = eHLoggerRefs.get(0).valueOf("@ref");

                        for (int i = 1; i < eHLoggerRefs.size(); i++) {
                            loggersRefValue += ", " + eHLoggerRefs.get(i).valueOf("@ref");
                        }

                        log4jProperties.setProperty(errorHandlerName + ".logger-ref", loggersRefValue);
                    }

                    //EH appender-ref, 0-1
                    Node appenderRef = errorHandler.selectSingleNode("./appender-ref");

                    if (appenderRef != null) {
                        log4jProperties.setProperty(errorHandlerName + ".appender-ref", appenderRef.valueOf("@ref"));
                    }
                }

                //appender params, optional
                List<Node> appenderParams = appender.selectNodes("./param");

                if (!appenderParams.isEmpty()) {
                    for (Node param : appenderParams) {
                        log4jProperties.setProperty(appenderName + "." + param.valueOf("@name"),
                                param.valueOf("@value"));
                    }
                }

                //appender layout, 0-1
                Node appenderLayout = appender.selectSingleNode("./layout");

                if (appenderLayout != null) {
                    String appenderLayoutName = appenderName + ".layout";

                    log4jProperties.setProperty(appenderLayoutName, appenderLayout.valueOf("@class"));

                    //AL params, optional
                    List<Node> appenderLayoutParams = appenderLayout.selectNodes("./param");

                    if (!appenderLayoutParams.isEmpty()) {
                        for (Node param : appenderLayoutParams) {
                            log4jProperties.setProperty(appenderLayoutName + "." + param.valueOf("@name"),
                                    param.valueOf("@value"));
                        }
                    }
                }

                //appender filter, optional
                List<Node> appenderFilters = appender.selectNodes("./filter");

                if (!appenderFilters.isEmpty()) {
                    String appenderFilterName = "";
                    int filterCount = 0;

                    for (Node filter : appenderFilters) {
                        filterCount++;
                        appenderFilterName = appenderName + ".filter." + filterCount;

                        log4jProperties.setProperty(appenderFilterName, filter.valueOf("@class"));

                        //filter params, optional
                        List<Node> filterParams = filter.selectNodes("./param");

                        if (!filterParams.isEmpty()) {
                            for (Node param : filterParams) {
                                log4jProperties.setProperty(appenderFilterName + "." + param.valueOf("@name"),
                                        param.valueOf("@value"));
                            }
                        }
                    }
                }

                //appender appender-refs, optional
                List<Node> appenderAppenderRefs = appender.selectNodes("./appender-ref");

                if (!appenderAppenderRefs.isEmpty()) {
                    String appenderRefsValue = appenderAppenderRefs.get(0).valueOf("@ref");

                    for (int i = 1; i < appenderAppenderRefs.size(); i++) {
                        appenderRefsValue += ", " + appenderAppenderRefs.get(i).valueOf("@ref");
                    }

                    log4jProperties.setProperty(appenderName + ".appender-ref", appenderRefsValue);
                }
            }
        }

        //category, optional
        List<Node> categoryNodes = doc.selectNodes("/log4j:configuration/category");

        if (!categoryNodes.isEmpty()) {
            for (Node category : categoryNodes) {

                String categoryContent = "";

                //(priority | level)?
                Node categoryPriority = category.selectSingleNode("./priority");
                Node categoryLevel = category.selectSingleNode("./level");

                if (categoryPriority != null) {
                    categoryContent += categoryPriority.valueOf("@value").toUpperCase();
                } else if (categoryLevel != null) {
                    categoryContent += categoryLevel.valueOf("@value").toUpperCase();
                }

                //appender-refs, optional
                List<Node> categoryAppenderRefs = category.selectNodes("./appender-ref");

                if (!categoryAppenderRefs.isEmpty()) {
                    for (Node appenderRef : categoryAppenderRefs) {
                        categoryContent += ", " + appenderRef.valueOf("@ref");
                    }
                }

                String categoryName = "log4j.category." + category.valueOf("@name");
                log4jProperties.setProperty(categoryName, categoryContent);

                //params, optional
                List<Node> categoryParams = category.selectNodes("./param");

                if (!categoryParams.isEmpty()) {
                    for (Node param : categoryParams) {
                        log4jProperties.setProperty(categoryName + "." + param.valueOf("@name"),
                                param.valueOf("@value"));
                    }
                }

                //category additivity
                if (category.valueOf("@additivity").equals("false")) {
                    log4jProperties.setProperty("log4j.additivity." + category.valueOf("@name"),
                            "false");
                }
            }
        }

        //logger, optional
        List<Node> loggerNodes = doc.selectNodes("/log4j:configuration/logger");

        if (!loggerNodes.isEmpty()) {
            for (Node logger : loggerNodes) {

                String loggerContent = "";

                //level, 0-1
                Node loggerLevel = logger.selectSingleNode("./level");

                if (loggerLevel != null) {
                    loggerContent += loggerLevel.valueOf("@value").toUpperCase();
                }

                //appender-refs, optional
                List<Node> loggerAppenderRefs = logger.selectNodes("./appender-ref");

                if (!loggerAppenderRefs.isEmpty()) {
                    for (Node appenderRef : loggerAppenderRefs) {
                        loggerContent += ", " + appenderRef.valueOf("@ref");
                    }
                }

                String loggerName = "log4j.logger." + logger.valueOf("@name");
                log4jProperties.setProperty(loggerName, loggerContent);

                //logger additivity
                if (logger.valueOf("@additivity").equals("false")) {
                    log4jProperties.setProperty("log4j.additivity." + logger.valueOf("@name"), "false");
                }
            }
        }
    }

    @Override
    public void convert(String filename) {

        load(filename);
        convert();
    }

    @Override
    public void saveTo(String filename) {

        if (log4jProperties.isEmpty()) {
            throw new IllegalStateException("There are no converted properties to be saved");
        }

        try (PrintStream output = new PrintStream(new FileOutputStream(filename, false))) {

            log4jProperties.list(output);

        } catch (FileNotFoundException ex) {
            log.error("Directory not found; Invalid path", ex);
            throw new IllegalArgumentException("Directory not found; Invalid path", ex);
        }

    }

    @Override
    public void convertAndSave(String inputFileName, String outputFileName) {

        load(inputFileName);
        convert();
        saveTo(outputFileName);

    }

    private void addDTDDeclaration(String filename) throws XMLStreamException {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent dtd = eventFactory
                .createDTD("<!DOCTYPE log4j:configuration SYSTEM \"" + tempDTD.getAbsolutePath() + "\">");

        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();

        XMLEventReader reader = inFactory
                .createXMLEventReader(new StreamSource(filename));
        reader = new DTDReplacer(reader, dtd);
        XMLEventWriter writer = outFactory.createXMLEventWriter(documentStream);
        writer.add(reader);
        writer.flush();

        writer.close();
    }

    private Document parse(URL url) throws DocumentException {
        /*SAXReader reader = new SAXReader();
         Document document = reader.read(url);
         return document;*/

        SAXReader reader = new SAXReader();
        reader.setValidation(true);
        reader.setErrorHandler(new SimpleErrorHandler());
        return reader.read(url);
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private class SimpleErrorHandler implements ErrorHandler {

        public void warning(SAXParseException e) throws SAXException {
            log.warn(e.getMessage());
            throw new IllegalArgumentException("Invalid input file content; Error parsing input file", e);
        }

        public void error(SAXParseException e) throws SAXException {
            log.error(e.getMessage());
            throw new IllegalArgumentException("Invalid input file content; Error parsing input file", e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            log.fatal(e.getMessage());
            throw new IllegalArgumentException("Invalid input file content; Error parsing input file", e);
        }
    }

    private class DTDReplacer extends EventReaderDelegate {

        private final XMLEvent dtd;
        private boolean sendDtd = false;

        public DTDReplacer(XMLEventReader reader, XMLEvent dtd) {
            super(reader);
            if (dtd.getEventType() != XMLEvent.DTD) {
                throw new IllegalArgumentException("" + dtd);
            }
            this.dtd = dtd;
        }

        @Override
        public XMLEvent nextEvent() throws XMLStreamException {
            if (sendDtd) {
                sendDtd = false;
                return dtd;
            }
            XMLEvent evt = super.nextEvent();
            if (evt.getEventType() == XMLEvent.START_DOCUMENT) {
                sendDtd = true;
            } else if (evt.getEventType() == XMLEvent.DTD) {
                // discard old DTD
                return super.nextEvent();
            }
            return evt;
        }

    }

    private class LinkedProperties extends Properties {

        private Map<Object, Object> linkMap;

        public LinkedProperties() {
            linkMap = new LinkedHashMap<Object, Object>();
        }

        @Override
        public synchronized Object setProperty(String key, String value) {
            return linkMap.put(key, value);
        }

        @Override
        public void list(PrintStream out) {
            for (Object key : linkMap.keySet()) {
                out.println(key.toString() + "=" + linkMap.get(key));
            }
        }

        @Override
        public boolean isEmpty() {
            return linkMap.isEmpty();
        }

        //---------------------------
        @Override
        public synchronized Object put(Object key, Object value) {
            return linkMap.put(key, value);
        }

        @Override
        public synchronized boolean contains(Object value) {
            return linkMap.containsValue(value);
        }

        @Override
        public boolean containsValue(Object value) {
            return linkMap.containsValue(value);
        }

        @Override
        public synchronized Enumeration<Object> elements() {
            throw new UnsupportedOperationException(
                    "Enumerations are so old-school, don't use them, "
                    + "use keySet() or entrySet() instead");
        }

        @Override
        public synchronized void clear() {
            linkMap.clear();
        }

        @Override
        public synchronized boolean containsKey(Object key) {
            return linkMap.containsKey(key);
        }

    }
}
