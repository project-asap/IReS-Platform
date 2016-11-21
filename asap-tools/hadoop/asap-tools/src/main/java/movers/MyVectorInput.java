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
public interface MyVectorInput {
    public Pair<String, Integer> nextDictEntry() throws IOException;    
    public MySparseVector nextVector() throws IOException;
}
