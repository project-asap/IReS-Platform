import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Writable;


public class SequenceInfo {

    public static void main(String[] argv) throws IOException, InstantiationException, IllegalAccessException {
        Configuration conf;
        conf = new Configuration();
        Path in = new Path(argv[0]);
        Reader reader = new SequenceFile.Reader(conf, Reader.file(in));
       
        Class keyClass=reader.getKeyClass(), valueClass=reader.getValueClass();
        System.out.println("KeyClass:"+ keyClass+"\nValueClass: "+valueClass);
        Writable key=(Writable)keyClass.newInstance();
        Writable value = (Writable) valueClass.newInstance();
        int counter=0;
        while (reader.next(key,value)) counter++;
        System.out.println("Lenght: "+counter);
    }
}