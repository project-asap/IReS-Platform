package movers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public class ArffInput implements MyVectorInput{

    BufferedReader reader;
    int termCount=0, vectorCount=0;
    
    public ArffInput(String filePath) throws FileNotFoundException{
        reader =  new BufferedReader(new FileReader(filePath));        
    }
    
    public static MySparseVector ArffString2Vector(String s) {
        int valueIdx = 1, spaceIdx, comaIdx;
        int vectorIndex;
        double value;
        boolean done = false;

        List<Integer> indexes = new ArrayList();
        List<Double> values = new ArrayList();

        //read all pairs of index/value
        while (!done) {
            //find the vector index of this value
            spaceIdx = s.indexOf(" ", valueIdx);
            
            //corner-case: empty vector
            if(spaceIdx==-1) break;
            
            vectorIndex = Integer.parseInt(s.substring(valueIdx, spaceIdx));

            //find the value
            comaIdx = s.indexOf(",", spaceIdx + 1);
            if (comaIdx == -1) {
                done = true;
                comaIdx = s.length() - 1;
            }
            value = Double.parseDouble(s.substring(spaceIdx + 1, comaIdx));

            //keep the index/value
            indexes.add(vectorIndex);
            values.add(value);

            //move the index to the next value
            valueIdx = comaIdx + 1;
        }
        return new MySparseVector(indexes, values);
    }
    
    @Override
    public Pair<String, Integer> nextDictEntry() throws IOException {
        String line, term;
        while((line=reader.readLine())!=null){
            if(!line.startsWith("@attribute"))
               if(line.startsWith("@data")) break; //end of dictinary
               else continue;
            if(line.contains("@@class@@")) continue;
            
            //handle the new term
            termCount++; //new term
            
            //strip the term
            int beginningOfTerm=11; // "@attribute" is 10 chars long, we will strip this and the following whitespace
            int endOfTerm=line.lastIndexOf("numeric")-1;
            term = line.substring(beginningOfTerm, endOfTerm);
            //return the new term with its id
            return new Pair(term, termCount);            
            
        }
        return null;
    }

    @Override
    public MySparseVector nextVector() throws IOException {
        
        String line;
        //read the vectors
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("{")) {
                continue;
            }
            vectorCount++;
            //construct the tfidf vector
            return ArffString2Vector(line);
        }
        return null;
    }
    
}
