package movers;

import java.io.IOException;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public class DummyOutput implements MyVectorOutput{

    @Override
    public void writeDictEntry(Pair<String, Integer> entry) throws IOException {
        System.out.println(entry.getFirst()+":"+entry.getSecond());
    }

    @Override
    public void writeVector(MySparseVector vector) throws IOException {
        System.out.println(vector);
    }

    @Override
    public void close() throws IOException {
    }
    
}
