package movers;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import static org.apache.hadoop.io.SequenceFile.createWriter;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.VectorWritable;

/**
 *
 * @author cmantas
 */
public class MahoutOutput implements MyVectorOutput {

    //writer for the term dictionary

    private Writer dictWriter;
    private Writer tfidfWriter;

    private int docId = 0;

    int termcount = 0;

    public MahoutOutput(String output, FileSystem fs, Configuration conf) throws IOException {

        //clear the output dir
        Path basedir = new Path(output);
        if (fs.exists(basedir)) {
            fs.delete(basedir, true); //Delete existing Directory
        }
        fs.mkdirs(basedir);

        String dictOutput = output + "/dictionary.file";
        dictWriter = createWriter(conf, Writer.file(new Path(dictOutput)),
                Writer.keyClass(Text.class), Writer.valueClass(IntWritable.class));

        String vectorsPath = output + "/tfidf-vectors";
        tfidfWriter = new SequenceFile.Writer(fs, conf,
                new Path(vectorsPath), Text.class, VectorWritable.class);
    }

    @Override
    public void writeDictEntry(Pair<String, Integer> entry) throws IOException {
        dictWriter.append(new Text(entry.getFirst()), new IntWritable(entry.getSecond()));
        termcount++;
    }

    @Override
    public void writeVector(MySparseVector vector) throws IOException {
        String docIdName = String.format("%09d", ++docId); //mahout stores doc ids as Text
        RandomAccessSparseVector vec = new RandomAccessSparseVector(termcount);
        for (int i = 0; i < vector.indices.size(); i++)
            vec.setQuick(vector.indices.get(i), vector.values.get(i));

        NamedVector nv = new NamedVector(vec, docIdName);
        tfidfWriter.append(new Text(docIdName), new VectorWritable(nv));
    }

    @Override
    public void close() throws IOException {
        dictWriter.close();
    }

}
