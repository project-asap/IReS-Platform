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

import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class OutputSpacePoint  {
    
    private InputSpacePoint inputSpacePoint;
    private HashMap<String,Double> outputPoints;
    private double value;
    private String key;


	public OutputSpacePoint() {
		outputPoints = new HashMap<String, Double>();
	}
	
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public void setValue(String key, Double value) {
        this.setKey(key);
        this.setValue(value);
    }

    public InputSpacePoint getInputSpacePoint() {
        return inputSpacePoint;
    }

    public void setInputSpacePoint(InputSpacePoint inputSpacePoint) {
        this.inputSpacePoint = inputSpacePoint;
    }

    @Override
    public String toString() {
    	String ret = "";
    	ret+=this.inputSpacePoint.toString()+" -> (";
    	for( Entry<String, Double> e : outputPoints.entrySet()){
    		ret+=e.getValue()+", ";
    	}
    	ret+=")";
        return ret;
    }

    public String toCSVString(String delimiter) {
    	String ret = "";
    	ret+=this.inputSpacePoint.toStringCSVFormat(",")+delimiter;
    	int i=0;
    	for( Entry<String, Double> e : outputPoints.entrySet()){
    		ret+=e.getValue();
    		i++;
    		if(i<outputPoints.size()){
    			ret+=delimiter;
    		}
    	}
    	
        return ret;
    }

	public void setValues(HashMap<String, Double> values) {
		this.outputPoints = values;
	}

	public HashMap<String, Double> getOutputPoints() {
		return outputPoints;
	}

	public int numberDimensions() {
		return outputPoints.size();
	}
    
    
    
}
