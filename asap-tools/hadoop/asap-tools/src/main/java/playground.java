import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.hadoop.io.DoubleWritable;

/**
 *
 * @author cmantas
 */
public class playground {
     final static int maxFeatures = 10000000;
             
    public static void main(String args[]){
    
    RandomAccessSparseVector vector;
    NamedVector namedVector;
    double d[] =new double[maxFeatures];
    d[3]=1;
    d[100]=2;
    vector = new RandomAccessSparseVector(d.length);
    vector.assign(d);
    namedVector = new NamedVector(vector,"name");
        System.out.println(namedVector);
    
    }
}
