package cslab;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;;


public class HadoopProps {
    
    static{
        try {
            System.out.println("HELLOOOOOOO");
            readProps();
        } catch (Exception ex) {
            System.err.println("Could Not read input File");
        }
    }
    
    
    public static final String PROPS_FILE="hadoop_job.properties";
    public static String input,output;
    
    public static void readProps() throws FileNotFoundException, ClassNotFoundException{
            Properties props = new Properties();            
            FileInputStream is =new FileInputStream(PROPS_FILE);
        try {
            props.load(is);
            input = props.getProperty("input", "input");
            output = props.getProperty("output", "output");
            is.close();
        } catch (IOException ex) { }            
    }
    



public static void main(String args[]) throws Exception{
        readProps();
    
}

} 