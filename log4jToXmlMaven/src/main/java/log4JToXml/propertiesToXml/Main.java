package log4JToXml.propertiesToXml;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main
{

	private static final String PROPERTIES_NAME = "log4j4.properties";
	
	private Document doc;

	public static void main(String[] args) throws ParserConfigurationException, TransformerConfigurationException
	{
		try
		{
			Properties config = loadProperties(PROPERTIES_NAME);
			XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
		}
		catch(Exception ex)
		{
			System.out.println("Unable to load properties.");
		}
	}

	private static Properties loadProperties(String propertiesName)
	{
		Properties config = new Properties();
		InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(propertiesName);
		try
		{
			config.load(inputStream);
		}
		catch (IOException ex)
		{
			System.out.println(ex.getStackTrace());
			System.out.println("Could not load properties file.");
		}
		return config;
	}
}