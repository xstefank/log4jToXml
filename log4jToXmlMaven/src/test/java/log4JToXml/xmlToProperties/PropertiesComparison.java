package log4JToXml.xmlToProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for comparing two properties files
 * 
 * @author David Kratochvil
 */
public class PropertiesComparison {
    
    private File file1;
    private File file2;
    private Set<String> set1 = new HashSet<String>();
    private Set<String> set2 = new HashSet<String>();
    private Set<String> root1 = new HashSet<String>();
    private Set<String> root2 = new HashSet<String>();
    
    /**
     * Constructor for comparing two files
     * 
     * @param file1
     * @param file2 
     */
    public PropertiesComparison(File file1, File file2){
        this.file1 = file1;
        this.file2 = file2;
    }
    
    /**
     * Compares if two files are equal
     * 
     * @return true - files are equal; false - files are not equal
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public boolean compare() throws FileNotFoundException, IOException{
        read(file1, set1, root1);
        read(file2, set2, root2);
        
        
        if(!root1.equals(root2))
            return false;
        return set1.equals(set2);
    }
    /**
     * Fills set/root with file's lines/appenders for further comparing
     * 
     * @param file
     * @param set
     * @param root
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void read(File file, Set<String> set, Set<String> root) throws FileNotFoundException, IOException{
        InputStream is = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        String line = br.readLine();
        try{
            while(line.trim().isEmpty() || line.startsWith("#")){
                line = br.readLine();
            }
            if(line.startsWith("log4j.rootLogger=")){
                String[] appenders = line.split(", ");
                root.add(appenders[0].substring(17));
                for(int i = 1; i < appenders.length; i++){
                    root.add(appenders[i]);
                }
            }
            while(line != null){
                if(!line.trim().isEmpty() && !line.startsWith("#")){
                    set.add(line);
                }
                line = br.readLine();
            }
        }catch(IOException ex){
            throw new IOException(ex);
        }finally{
            is.close();
        }
    }
}
