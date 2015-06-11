/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package log4JToXml.propertiesToXml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Admin
 */
public class PropertiesToXmlTest
{

    public PropertiesToXmlTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void propertiesToXmlCorrectTest() throws IllegalArgumentException, ParserConfigurationException, IOException
    {
        Properties config = loadProperties("testData/my.properties");
        XmlPropertiesBuilder xml = new XmlPropertiesBuilder(config);
        xml.saveXmlDocument("testoutput1.xml");
        File output = new File("testoutput1.xml");
        File reference = new File("src/main/resources/testData/test01.xml");
        XMLComparison com = new XMLComparison(output, reference);
        assertTrue(com.compare());
    }

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
            System.out.println(ex.getStackTrace());
            System.out.println("Could not load properties file.");
        }
        return config;
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
