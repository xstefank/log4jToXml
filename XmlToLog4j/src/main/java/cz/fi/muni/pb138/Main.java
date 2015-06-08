/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.fi.muni.pb138;

import java.io.IOException;
import org.dom4j.DocumentException;

/**
 *
 * @author martin
 */
public class Main {

    public static void main(String[] args) throws IOException {

        XmlToLog4jConverter converter = new XmlToLog4jConverterImpl();

        try {
            converter.load("src/main/resources//testData/test01.xml");
//            converter.convertAndSave("../testData/test01.xml", "../testData/my.properties");
        } catch (IllegalArgumentException ex) {
            System.out.println("LOOOOOL EXCEPTION");
        }

        converter.convert();
        converter.saveTo("src/main/resources/testData/my.properties");

    }
}
