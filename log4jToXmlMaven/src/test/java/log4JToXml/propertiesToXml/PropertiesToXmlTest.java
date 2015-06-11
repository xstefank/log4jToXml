package log4JToXml.propertiesToXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing class for conversion from log4j.properties to log4j.xml
 * 
 * @author David Kratochvil
 * 
 */
public class PropertiesToXmlTest 
{
    /**
     * Tests correct log4j.properties
     * 
     * @throws IllegalArgumentException
     * @throws ParserConfigurationException
     * @throws IOException 
     */
    @Test
    public void propertiesToXmlTest1() throws IllegalArgumentException, ParserConfigurationException, IOException
    {
        Properties config = loadProperties("testData/propertiestest1.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("src/main/resources/testData/propertiesToXmlTestOutput1.xml");
        File output = new File("src/main/resources/testData/propertiesToXmlTestOutput1.xml");
        File reference = new File("src/main/resources/testData/xmltest1.xml");
        XMLComparison com = new XMLComparison(output, reference);
        assertTrue(com.compare());
    }
    
    /**
     * Tests correct log4j.properties without root
     * 
     * @throws IllegalArgumentException
     * @throws ParserConfigurationException
     * @throws IOException 
     */
    @Test
    public void propertiesToXmlTest2() throws IllegalArgumentException, ParserConfigurationException, IOException {
        Properties config = loadProperties("testData/propertiestest2.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("src/main/resources/testData/propertiesToXmlTestOutput2.xml");
        File output = new File("src/main/resources/testData/propertiesToXmlTestOutput2.xml");
        File reference = new File("src/main/resources/testData/xmltest2.xml");
        XMLComparison com = new XMLComparison(output, reference);
        assertTrue(com.compare());
    }
    
     /**
      * Tests incorrect log4j.properties with appender without class
      * 
      * @throws IOException 
      */
    @Test(expected = IllegalArgumentException.class)
    public void propertiesToXmlTest3() throws IOException {
        Properties config = loadProperties("testData/propertiestest3.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("src/main/resources/testData/propertiesToXmlTestOutput3.xml");
    }
    /**
     * Tests incorrect log4j.properties with parameter without value
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void propertiesToXmlTest4() throws IOException {
        Properties config = loadProperties("testData/propertiestest4.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("src/main/resources/testData/propertiesToXmlTestOutput4.xml");
    }
    
    /**
     * Tests incorrect log4j.properties with icorrectly placed parameter
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void propertiesToXmlTest5() throws IOException {
        Properties config = loadProperties("testData/propertiestest5.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("src/main/resources/tehwrstData/propertiesToXmlTestOutput5.xml");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullProperiesTest(){
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullSaveTest() throws IllegalArgumentException, ParserConfigurationException, IOException
    {
        Properties config = loadProperties("testData/propertiestest1.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void notAPropertieFileTest() throws IllegalArgumentException, ParserConfigurationException, IOException
    {
        Properties config = new Properties();
        InputStream is = new FileInputStream("src/main/resources/testData/xmltest1.xml");
        config.load(is);
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
    }
    
    /**
     * Loads log4j.properties with given path
     * 
     * @param propertiesName
     * @return 
     */
    private static Properties loadProperties(String propertiesName)
    {
        Properties config = new Properties();
        InputStream inputStream = PropertiesToXmlTest.class.getClassLoader().getResourceAsStream(propertiesName);
        try
        {
            config.load(inputStream);
        }
        catch (IOException ex)
        {
            System.out.println("Could not load properties file.");
        }
        return config;
    }
    
}
