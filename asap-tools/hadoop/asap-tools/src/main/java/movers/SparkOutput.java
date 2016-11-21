package movers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import static org.apache.hadoop.io.SequenceFile.createWriter;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public class SparkOutput implements MyVectorOutput{
    
    private BufferedWriter tfidfWriter; //the writer for the tfidf vectors
    private SequenceFile.Writer dictWriter; //the writer for the dictionary

    public SparkOutput(String output , FileSystem fs, Configuration conf) throws IOException{
        Path outPath = new Path(output);
        if (fs.exists(outPath)) {
            fs.delete(outPath, true);
        }
        
        fs.mkdirs(outPath);
        
        Path vectorsDir = new Path(output+"/vectors");
        tfidfWriter = new BufferedWriter(new OutputStreamWriter(fs.create(vectorsDir), "UTF-8"));
        
        
        String dictOutput = output+"/dictionary";
        dictWriter = createWriter(conf, SequenceFile.Writer.file(new Path(dictOutput)), 
                    SequenceFile.Writer.keyClass(Text.class),SequenceFile.Writer.valueClass(IntWritable.class));
    }
    
    public static String Vector2SparkString(MySparseVector vector){
        String rv = "";
        rv += "(1048576," + vector.indices + "," + vector.values + ")";
        return rv;
    }
    
    @Override
    public void writeDictEntry(Pair<String, Integer> entry) throws IOException {
        Text key = new Text(entry.getFirst());
        IntWritable value = new IntWritable(entry.getSecond());
        dictWriter.append(key, value);
    }

    @Override
    public void writeVector(MySparseVector vector) throws IOException {
        String vectorString = Vector2SparkString(vector);    
        tfidfWriter.append(vectorString+"\n");
    }

    @Override
    public void close() throws IOException {
        dictWriter.close();
        tfidfWriter.close();
    }
    
}
