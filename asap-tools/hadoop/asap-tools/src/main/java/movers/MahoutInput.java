package movers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import static org.apache.mahout.common.iterator.sequencefile.PathFilters.logsCRCFilter;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

/**
 *
 * @author cmantas
 */
public class MahoutInput implements MyVectorInput {
    
     Iterator<Pair<Text, VectorWritable>> vecIterator;
     Reader dirReader;
    
    public MahoutInput(String input , FileSystem fs, Configuration conf) throws IOException{
        
        String vectorsPath = input+"/tfidf-vectors";        
        vecIterator= (new SequenceFileDirIterable<Text, VectorWritable>(
                        new Path(vectorsPath), PathType.LIST, logsCRCFilter(), conf)
                      ).iterator();
        
        String dictPath = input + "/dictionary.file-0";
        dirReader = new SequenceFile.Reader(conf, Reader.file(new Path(dictPath)));
    }

    @Override
    public Pair<String, Integer> nextDictEntry() throws IOException {
        Text key = new Text();
        IntWritable val = new IntWritable();
        
        if(!dirReader.next(key, val)) return null;
        
        return new Pair(key.toString(), val.get());
    }

    @Override
    public MySparseVector nextVector() throws IOException {
       
        if(!vecIterator.hasNext()) return null;
        
        Pair<Text, VectorWritable> entry = vecIterator.next();
        
        String name = entry.getFirst().toString();
        VectorWritable mahoutVector = entry.getSecond();
        
        ArrayList<Integer> indices = new ArrayList();
        ArrayList<Double> values = new ArrayList();
        
        for(Element e: mahoutVector.get().all()){
            double value =e.get();
            if (value==0) continue;
            values.add(value);
            int index= e.index();
            indices.add(index);            
        }
        
        return new MySparseVector(indices, values);
        
    }
    
}
