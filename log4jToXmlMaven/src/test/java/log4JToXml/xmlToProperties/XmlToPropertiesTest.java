package log4JToXml.xmlToProperties;


import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing class for conversion from log4j.xml to log4j.properties
 * 
 * @author David Kratochvil
 */
public class XmlToPropertiesTest {
    
    /**
     * Tests xml to properties conversion
     * 
     * @throws IOException 
     */
    @Test
    public void xmlToPropertiesTest1() throws IOException{
        String input = "src/main/resources/testData/xmltest1.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput1.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
        File out = new  File("src/main/resources/testData/XmlToPropertiesTestOutput1.properties");
        File reference = new File("src/main/resources/testData/propertiestest1.properties");
        
        PropertiesComparison com = new PropertiesComparison(out, reference);
        assertTrue(com.compare());
    }
    
    /**
     * Tests xml to properties conversion without root
     * 
     * @throws IOException 
     */
    @Test
    public void xmlToPropertiesTest2() throws IOException{
        String input = "src/main/resources/testData/xmltest2.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput2.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
        File out = new  File("src/main/resources/testData/XmlToPropertiesTestOutput2.properties");
        File reference = new File("src/main/resources/testData/propertiestest2.properties");
        
        PropertiesComparison com = new PropertiesComparison(out, reference);
        assertTrue(com.compare());
    }
    
    /**
     * Tests xml to properties conversion without appender without class
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void xmlToPropertiesTest3() throws IOException {
        String input = "src/main/resources/testData/xmltest3.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput3.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests xml to properties conversion with parameter without value
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void xmlToPropertiesTest4() throws IOException {
        String input = "src/main/resources/testData/xmltest4.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput4.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests xml to properties conversion with icorrectly placed parameter
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void xmlToPropertiesTest5() throws IOException {
        String input = "src/main/resources/testData/xmltest5.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput5.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests methods convert(String) and saveTo(String)
     * 
     * @throws IOException 
     */
    @Test
    public void xmlToPropertiesTest6() throws IOException {
        String input = "src/main/resources/testData/xmltest1.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput12.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convert(input);
        xml.saveTo(output);
        File out = new  File("src/main/resources/testData/XmlToPropertiesTestOutput12.properties");
        File reference = new File("src/main/resources/testData/propertiestest1.properties");
        
        PropertiesComparison com = new PropertiesComparison(out, reference);
        assertTrue(com.compare());
    }
    
    /**
     * Tests xml to properties conversion without input file
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullXmlTest() throws IOException{
        String input = null;
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput1.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests xml to properties conversion without output file
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullSaveTest() throws IOException{
        String input = "src/main/resources/testData/xmltest1.xml";
        String output = null;
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests xml to properties conversion with nonexisting file
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nonexistentXmlTest() throws IOException{
        String input = "src/main/resources/testData/xmltest6.xml";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput1.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
    
    /**
     * Tests xml to properties conversion with non-xml file
     * 
     * @throws IOException 
     */
    @Test(expected = IllegalArgumentException.class)
    public void notAnXmlTest() throws IOException{
        String input = "src/main/resources/testData/propertiestest1.properties";
        String output = "src/main/resources/testData/XmlToPropertiesTestOutput1.properties";
        XmlToLog4jConverterImpl xml = new XmlToLog4jConverterImpl();
        xml.convertAndSave(input, output);
    }
}
