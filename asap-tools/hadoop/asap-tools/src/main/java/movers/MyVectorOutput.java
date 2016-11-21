package movers;

import java.io.IOException;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public interface MyVectorOutput {
    public void writeDictEntry(Pair<String, Integer> entry) throws IOException;    
    public void writeVector(MySparseVector vector) throws IOException;
    public void close() throws IOException;
}
