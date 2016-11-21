/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movers;

import java.io.IOException;
import org.apache.mahout.common.Pair;

/**
 *
 * @author cmantas
 */
public final class Mover {
    
    
    public static void move(MyVectorInput input, MyVectorOutput output) throws IOException{
        
        //move the dictionary
        Pair<String,Integer> dictEntry;
        int terms=0, vectors=0;
        
        while((dictEntry=input.nextDictEntry())!=null){
            output.writeDictEntry(dictEntry);
            terms++;
        }
        
        System.out.println("Moved "+terms+" dict terms");
        
        //move the vectors
        MySparseVector vector;
        while((vector=input.nextVector())!=null){
            output.writeVector(vector);
            vectors++;
        }
        
        output.close();
        
        System.out.println("Moved "+ vectors+ " document vectors");
    }
}
