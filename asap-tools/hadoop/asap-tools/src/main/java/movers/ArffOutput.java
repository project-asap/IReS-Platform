package movers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public class ArffOutput implements MyVectorOutput{
    
    Writer writer;
    boolean writenDataTag=false;

    public ArffOutput(String fname) throws UnsupportedEncodingException, IOException {

        //create the output Writer        
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname), "utf-8"));

        writePreface(writer); //write the preface text
    }
        
    public static void writePreface(Writer writer) throws IOException{
      String preface="@relation '_home_cmantas_Data_docs_virt_dir-weka.filters.unsupervised.attribute.StringToWordVector-R1-W9999999-prune-rate-1.0-C-N0-L-stemmerweka.core.stemmers.NullStemmer-M1-tokenizerweka.core.tokenizers.WordTokenizer -delimiters \\\" \\\\r\\\\n\\\\t.,;:\\\\\\'\\\\\\\"()?\\\\\\\\!$#-0123456789/*\\\\\\%<>@[]+`~_=&^   \\\"";
      preface +="\n\n@attribute @@class@@ {text}\n";
      writer.write(preface);
    }
    
    public static String Vector2ArffString(MySparseVector vector){
        String rv="";
         rv+="{";
         int count=vector.indices.size();
         for(int i=0; i<count-1; i++){
            //get the value
            double value =vector.values.get(i);
            int index = vector.indices.get(i);
            if (value==0) continue;;
            rv +=(index+1)+" "+value+",";
         }
         rv+=(vector.indices.get(count-1)+1)+" "+ vector.values.get(count-1)+"}";
        return rv;
    }

    @Override
    public void writeDictEntry(Pair<String, Integer> entry) throws IOException {
        writer.append("@attribute "+entry.getFirst() + " numeric\n");
    }
    
    
    private static Pair<Integer,Double> popMin(MySparseVector v){
        int minIndexIndex=-1, minIndexValue=Integer.MAX_VALUE;
        double minValue=-1;
        
        for(int i=0; i<v.size(); i++){
            if(minIndexValue>v.indices.get(i)){
                minIndexIndex=i;
                minIndexValue=v.indices.get(i);
                minValue=v.values.get(i);
            }
        }
        
        v.indices.remove(minIndexIndex);
        v.values.remove(minIndexIndex);
        return new Pair(minIndexValue, minValue);
        
    }
    
    private static MySparseVector ordered(MySparseVector v){
        MySparseVector rv = new MySparseVector();
        while(v.size()!=0)
            rv.add(popMin(v));

        return rv;
    }
    

    @Override
    public void writeVector(MySparseVector vector) throws IOException {
        
        if(!writenDataTag){
            writer.append("\n@data\n\n");
            writenDataTag = true;
        }
        
        vector = ordered(vector);        
        writer.append(Vector2ArffString(vector)+"\n");
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
    
    
}
