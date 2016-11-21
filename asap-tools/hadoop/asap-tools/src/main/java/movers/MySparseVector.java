/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.mahout.common.Pair;;

/**
 *
 * @author cmantas
 */
public class MySparseVector {
        List<Integer> indices;
        List<Double> values;
        
    public MySparseVector(List<Integer> intexes, List<Double> values) {
        this.indices = intexes;
        this.values = values;
    }

    MySparseVector() {
        this.indices = new ArrayList();
        this.values = new ArrayList();
    }
        
    
  public MySparseVector rebase(Map<Integer, Integer> map){
        List<Integer> newIndices = new LinkedList();
        for (Integer index: this.indices)
            newIndices.add(map.get(index));
        
        MySparseVector rv =  new MySparseVector(newIndices, this.values);
        return rv;
    }
    
    @Override
    public String toString(){
        String rv="";
         rv+="{";
         int count=indices.size();
         for(int i=0; i<count-1; i++){
            //get the value
            double value =values.get(i);
            int index = indices.get(i);
            if (value==0) continue;;
            rv +=(index+1)+":"+value+",";
         }
         rv+=(indices.get(count-1)+1)+" "+ values.get(count-1)+"}";
        return rv;
    }
    
    public void add(int index, double value){
        indices.add(index);
        values.add(value);        
    }
    
    public void add(Pair<Integer,Double> entry){
        this.add(entry.getFirst(), entry.getSecond());
    }
    
    public int size(){
        return this.indices.size();
    }
    
    
}
