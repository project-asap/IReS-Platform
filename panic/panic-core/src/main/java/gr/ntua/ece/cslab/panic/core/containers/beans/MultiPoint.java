/*
 * Copyright 2014 Giannis Giannakopoulos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.panic.core.containers.beans;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Represents a multidimensional point. The dimensions are indexed using a key
 * (String) and each dimension is mapped to an applicable value (double).
 * @author Giannis Giannakopoulos
 */
public class MultiPoint implements Serializable{
    
    private static final String doubleFormat = "0.0000000000";
    private static final long serialVersionUID = 1L;
    private HashMap<String, Double> values;

    public MultiPoint() {
        this.values = new HashMap<String, Double>();
    }
   
    public HashMap<String, Double> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Double> values) {
        for(String key : values.keySet()) {
            values.put(key, chopDouble(values.get(key)));
        }
        this.values = values;
    }

    public void addDimension(String key, Double value) {
        this.values.put(key, chopDouble(value));
    }
    
    public Double getValue(String key) {
        return this.values.get(key);
    }
    
    public Collection<Double> getValuesAsCollection() {
        return this.values.values();
    }
    
    public Collection<String> getKeysAsCollection() {
        return this.values.keySet();
    }
    
    public int numberDimensions(){
        return this.values.size();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = super.equals(o);
        MultiPoint other;
        if(o instanceof MultiPoint) 
            other = (MultiPoint) o;
        else
            return result;
        
        for(String key : this.getKeysAsCollection()) {
            if((other.getValue(key) == null) || !other.getValue(key).equals(this.getValue(key)))
                return false;
        }
        return (other.getKeysAsCollection().size() == this.getKeysAsCollection().size());
    }

    @Override
    public int hashCode() {
        ByteBuffer buffer = ByteBuffer.allocate(this.values.size() * Double.SIZE/8);
        for(String key : this.getKeysAsCollection())
            buffer.putDouble(this.getValue(key));
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(buffer);
            byte[] md5 = digest.digest(buffer.array());
            ByteBuffer buf = ByteBuffer.wrap(md5);
            return buf.getInt();
            
        } catch (NoSuchAlgorithmException e) {
//            Logger.getGlobal().
        }
        return 1;
    }
    
    private Double chopDouble(Double initial) {
        return new Double(new DecimalFormat(doubleFormat).format(initial));
        
    }
}
