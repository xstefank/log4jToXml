/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.fi.muni.pb138;

/**
 *
 * @author xstefank
 * @version 1.0
 */
public interface XmlToLog4jConverter {
    
    /**
     * Method loads the log4j data from the XML file,
     * it is also responsible for the validity of the input
     * 
     * @param filename path to the desired XML file
     * @throws IllegalArgumentException if there is any problem opening / validating the input XML file
     */
    void load(String filename);
    
    /**
     * Method converts loaded input XML file to log4j properties format,
     * 
     * @throws IllegalStateException if there is no data loaded to be converted
     */
    void convert();
    
    /**
     * Method loads and validate the input XML file and converts it to it's 
     * log4j properties representation
     * 
     * @param filename path to the desired XML file
     * @throws IllegalArgumentException if there is any error opening / validating the input XML file
     */
    void convert(String filename);
    
    /**
     * Method saves the converted data to the desired file
     * 
     * @param filename path to the file where the properties will be saved,
     * if the file exists, it will be overridden, otherwise it will be created
     * @throws IllegalStateException if there are no properties converted
     * @throws  IllegalArgumentException if there is no writing right to the desired directory
     * or the directory does not exists
     */
    void saveTo(String filename);
    
    /**
     * Method loads and validates input XML file, converts it to it's log4j properties
     * representation and saves it to the file specified
     * 
     * @param inputFileName path to the input XML file
     * @param outputFileName path to the desired output file
     * @throws IllegalArgumentException if there is any problem opening / validating the input XML file or
     * if there is problem writing / creting the output file
     */
    void convertAndSave(String inputFileName, String outputFileName);
    
}
