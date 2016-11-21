import java.io.File;
import java.io.IOException;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import static java.util.Arrays.sort;
import mahout_stollen.ChunkedWriter;
import org.apache.hadoop.fs.FileSystem;

/**
 *
 * @author cmantas
 */
public class LocalSeqDirectory {
    
    
    public static void main(String[] args) throws IOException{
        
        //default values
        int maxCount=0, chunkSize =1984;
        
        //args parsing
        String localDirPath = args[0];
        String distrSeqDirPath = args[1];
        if(args.length>2)
            if (!args[2].equals("all"))
                maxCount=Integer.parseInt(args[2]);
        if(args.length>3)
            chunkSize=Integer.parseInt(args[3]);
            
        File localDir = new File(localDirPath);
        Path seqFilePath = new Path(distrSeqDirPath);
        
        Configuration conf = new Configuration();
        
        //delete output files
        (FileSystem.get(conf)).delete(seqFilePath, true);
        
        //init the Writer
        ChunkedWriter writer = new ChunkedWriter(new Configuration(),chunkSize,seqFilePath);
        
        //get files and sort
        File[] allFiles = localDir.listFiles();
        sort(allFiles);
        
        //iterate through files (shallow)
        int count = 0;
        for (File f : allFiles) {
            if (f.isFile()) {
                String contents = new String(readAllBytes(get(f.getAbsolutePath())));
                String baseName = f.getName();
                writer.write(baseName, contents);
                if (((count += 1) >= maxCount) && (maxCount != 0)) break;
            }
        }
        
        writer.close();
        System.out.println("Read "+ count+" files");
        //close the seqFile writer
        
                
                
    }
}
