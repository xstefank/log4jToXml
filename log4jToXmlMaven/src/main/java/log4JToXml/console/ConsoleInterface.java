package log4JToXml.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import log4JToXml.gui.Gui;
import log4JToXml.propertiesToXml.XmlPropertiesBuilder;
import log4JToXml.xmlToProperties.XmlToLog4jConverter;
import log4JToXml.xmlToProperties.XmlToLog4jConverterImpl;
import org.apache.log4j.Logger;

/**
 * Console interface for log4jtoxml 
 *
 * @author Lukas Linhart
 * @version 1.0
 */
public class ConsoleInterface
{

    private String input = null;
    private String output = null;
    private boolean toXml = false;
    private boolean toProperties = false;
    private boolean graphical = false;
    private static final Logger log = Logger.getLogger(XmlToLog4jConverterImpl.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ConsoleInterface logXML = new ConsoleInterface();
        logXML.parseArgs(args);
        logXML.setOutputFile();
        if (logXML.graphical)
        {
            Gui.main(args);
        }
        else
        {
            if (logXML.toXml && logXML.toProperties)
            {
                System.err.println("to many arguments (both -x and -p are used), cannot convert file, use -h to help");
                System.exit(1);
            }
            else if (!logXML.toXml && !logXML.toProperties)
            {
                System.err.println("to few arguments, cannot conert file, use -h to help");
                System.exit(1);
            }
            else if (logXML.toXml)
            {
                logXML.convertToXML();
            }
            else if (logXML.toProperties)
            {
                logXML.convertToProperties();
            }
        }
    }

    private void convertToProperties()
    {
        XmlToLog4jConverter converter = null;
        try
        {
            checkInputFile();
            converter = new XmlToLog4jConverterImpl();
        }
        catch (IOException ex)
        {
            log.error("Cannot create temporary file in your directory", ex);
            System.err.println("bla perm");
            System.exit(1);
        }

        try
        {
            converter.convert(input);
        }
        catch (IllegalArgumentException ex)
        {
            log.error("Input file is not valid xml", ex);
            System.err.println("Input file is not valid xml file.");
            System.exit(1);
        }

        converter.saveTo(output);
    }

    private void convertToXML()
    {
        checkInputFile();
        Properties properties = new Properties();
        File f = new File(input);
        try
        {
            InputStream is = new FileInputStream(f);
            properties.load(is);
        }
        catch (IOException ex)
        {
            log.error("Cannot load file", ex);
            System.err.println("Input file couldn't be open.");
            System.exit(1);
        }
        XmlPropertiesBuilder propertiesBuilder = new XmlPropertiesBuilder(properties);
        try
        {
            propertiesBuilder.saveXmlDocument(output);
        }
        catch (IOException ex)
        {
            log.error("Cannot save file", ex);
            System.err.println("output file couldn't be save.");
            System.exit(2);
        }
    }

    private void checkInputFile()
    {
        File inputFile = new File(input);
        if (!inputFile.exists())
        {
            System.err.println("Input file " + input + " doesn't exist.");
            System.exit(2);
        }
        if (inputFile.isDirectory())
        {
            System.err.println("Input file " + input + " is directory.");
            System.exit(2);
        }
        if (!inputFile.canRead())
        {
            System.err.println("Input file " + input + " cannot read.");
            System.exit(2);
        }
    }

    private void help()
    {
        System.out.println("Log4jToXML is program for converting log4j configuration"
                + "from Java properties file to xml and vice versa.\n"
                + "usage: Log4jToXml [-h] -x/-p inputfile [-n outputfile]\n\n"
                + "For converting from propertie file to xml use -p argument\n"
                + "Syntax: -p filepath\n"
                + "For converting from "
                + "xml to propertie file use -x argument\n"
                + "Syntax: -x filepath\n\n"
                + "For rename or change path of output file use -n argument\n"
                + "Syntax: -n filepath/name\n"
                + "For launch graphical application use -g argument\n"
                + "Syntax: -g\n"
                + "if not used ouput file will be in same directory where is input file with same name\n"
                + "Use -h to display this description");
        System.exit(0);
    }

    private void parseArgs(String args[])
    {
        int i = 0;
        while (i < args.length)
        {
            switch (args[i])
            {
                case "-h":
                    help();
                    break;
                case "-g":
                    graphical = true;
                    return;
                case "-x":
                    i++;
                    input = args[i];
                    toProperties = true;
                    break;
                case "-p":
                    i++;
                    input = args[i];
                    toXml = true;
                    break;
                case "-n":
                    i++;
                    output = args[i];
                    break;
                default:
                    System.err.println(args[i] + " is not a right argument, use -h to help");
                    System.exit(1);
            }
            i++;
        }
    }

    private void setOutputFile()
    {
        if (output == null)
        {
            String end;
            if (toXml)
            {
                end = ".xml";
            }
            else
            {
                end = ".properties";
            }
            output = input + end;
        }
    }
}
