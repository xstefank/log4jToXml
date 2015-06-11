package log4JToXml.propertiesToXml;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Basic class for comparing two xml files. Comparing is case sensitive.
 *
 * @author Lukas Linhart
 * @version 1.0
 * @
 */
public class XMLComparison
{

    private Document document1;
    private Document document2;
    private Set<String> set1 = new TreeSet<>();
    private Set<String> set2 = new TreeSet<>();

    /**
     * Constructor for basic comparing 2 xml files
     *
     * @param document1 
     * @param document2
     * @throws IllegalArgumentException
     * @throws ParserConfigurationException
     */
    public XMLComparison(File document1, File document2) throws IllegalArgumentException, ParserConfigurationException
    {
        checkFile(document1);
        checkFile(document2);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        try
        {
            this.document1 = dBuilder.parse(document1);
        }
        catch (SAXException | IOException ex)
        {
            throw new IllegalArgumentException("Document1 is not xml file", ex);
        }
        try
        {
            this.document2 = dBuilder.parse(document2);
        }
        catch (SAXException | IOException ex)
        {
            throw new IllegalArgumentException("Document2 is not xml file", ex);
        }
    }

    /**
     * Method compares xml files
     *
     * @return true - xml files are equal, false - files are not equal
     * @throws ParserConfigurationException
     */
    public boolean compare() throws ParserConfigurationException
    {
        Element root1 = document1.getDocumentElement();
        Element root2 = document2.getDocumentElement();
        paths(root1, root1.getNodeName(), set1);
        paths(root2, root2.getNodeName(), set2);
        
        return set1.equals(set2);
    }

    /**
     * Convert XML into set
     * 
     * @param element
     * @param path
     * @param set 
     */
    private void paths(Element element, String path, Set<String> set)
    {
        NamedNodeMap atr = element.getAttributes();
        StringBuilder newPath = new StringBuilder(path);
        if (atr.getLength() > 0) // atributes of element
        {
            newPath.append("[");
            for (int i = 0; i < atr.getLength(); i++)
            {
                newPath.append("@").append(atr.item(i).getNodeName()).append("=").
                        append(atr.item(i).getNodeValue()).append(";");
            }
            newPath.append("]");
        }
        set.add(newPath.toString()); //element with atributes to set
        NodeList nodes = element.getChildNodes(); 
        for (int i = 0; i < nodes.getLength(); i++) // child elements
        {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE)
            {
                paths((Element) nodes.item(i), newPath + "/" + nodes.item(i).getNodeName(), set);
            }
            if (nodes.item(i).getNodeType() == Node.TEXT_NODE) //text nodes 
            {
                String s = nodes.item(i).getTextContent().trim(); //trim for deleteng white space
                if (s.length() == 0) // only whitespace string
                {
                    continue;
                }
                set.add(newPath + "=" + s);
            }
        }
    }
    /**
     * Checks if file is correct
     * 
     * @param file 
     */
    private void checkFile(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("File is null");
        }
        if(!file.exists())
        {
           throw new IllegalArgumentException("File" + file.getName() + "doesn't exist");
        }
        if(file.isDirectory())
        {
            throw new IllegalArgumentException("File " + file.getName() + " is directory." );
        }
        if(!file.canRead())
        {
            throw new IllegalArgumentException("File " + file.getName() + " cannot read." );
        }
    }
}
