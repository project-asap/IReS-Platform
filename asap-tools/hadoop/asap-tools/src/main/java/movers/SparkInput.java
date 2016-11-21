/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import static org.apache.mahout.common.iterator.sequencefile.PathFilters.logsCRCFilter;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;

/**
 *
 * @author cmantas
 */
public class SparkInput implements MyVectorInput{
    
    Configuration conf;
    
    int termCount = 0;
    
    // the mapping between real (hashed) to fake (sequential) indices
    HashMap<Integer, Integer> indexMap = new HashMap();
    
   private final Iterator<Pair<Text, IntWritable>> dictionaryIterator;
   
   BufferedReader br;
   
   FileSystem fs;
    
   FileStatus[] inputVectorFiles;
   int currentFile;
   
   int tries=0;
   
   
   public SparkInput(String input, FileSystem fs, Configuration conf) throws IOException{
        this.conf=conf;
        this.fs = fs;
        String dictPath = input+"/dictionary";        
        SequenceFileDirIterable<Text, IntWritable> dictionaryIterable =
                new SequenceFileDirIterable<>(new Path(dictPath), PathType.LIST, logsCRCFilter(), conf);
        dictionaryIterator=dictionaryIterable.iterator();
        
        inputVectorFiles = fs.listStatus(new Path(input));
        currentFile = -1;
        nextFile();
    }
    
    
    private boolean nextFile() throws IOException{
        for(++currentFile; currentFile<inputVectorFiles.length;currentFile++){
          FileStatus f = inputVectorFiles[currentFile];  
          if (!f.isFile())continue;
          br = new BufferedReader(new InputStreamReader(fs.open(f.getPath())));
          return  true;
        }
        return false;
    }
    
    
    public static MySparseVector fromSparkString(String s) {
        List<Integer> indexes = new ArrayList();
        List<Double> values = new ArrayList();

        //the start index (inside the string) of the indexes of the vector's values
        int startOfIndexes = s.indexOf('[');
        //the end of indexes
        int endOfIndexes = s.indexOf(']', startOfIndexes + 1);

        //the start and end  index of values
        int startOfValues = s.indexOf('[', endOfIndexes + 1);
        int endOfValues = s.indexOf(']', startOfValues + 1);
        
        //read indexes
        String toConsume = s.substring(startOfIndexes+1, endOfIndexes);
        int prevDelim = -1, nextDelim=toConsume.indexOf(',');
        do{
            String sValue = toConsume.substring(prevDelim+1, nextDelim);
            indexes.add( Integer.valueOf(sValue));
            prevDelim = nextDelim; 
            nextDelim = toConsume.indexOf(',', prevDelim+1);               
        }while(nextDelim !=-1);

        //read values
        toConsume = s.substring(startOfValues+1, endOfValues);
        prevDelim = -1; nextDelim=toConsume.indexOf(',');
        do{
            String sValue = toConsume.substring(prevDelim+1, nextDelim);
            values.add( Double.valueOf(sValue));
            prevDelim = nextDelim; 
            nextDelim = toConsume.indexOf(',', prevDelim+1);                        
        }while(nextDelim !=-1);

        return new MySparseVector(indexes, values);
    }
    
    @Override
    public Pair<String, Integer> nextDictEntry(){
        if(!dictionaryIterator.hasNext()) return null;
        Pair<Text, IntWritable> p = dictionaryIterator.next();
        int realIndex = p.getSecond().get();
        String value = p.getFirst().toString();
        indexMap.put(realIndex, termCount);
        termCount++;
        return new Pair(value, termCount);
    }
    
    @Override
    public MySparseVector nextVector() throws IOException{
        String line;
        
        //if there are no more lines in the current file move to the next file
        while((line = br.readLine())==null){
            if(!nextFile()) return null;
        }
        tries++;
        //read a vector
        try{
        MySparseVector vec = fromSparkString(line);
        
        //move the vector's indices to consecutive space
        return vec.rebase(indexMap);
        }catch(Exception e){
            if(tries<5) return nextVector();
            else return null;
        }
    }

    
}
