/*
 * Copyright 2016 ASAP.
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


package gr.ntua.cslab.asap.operators;

import gr.ntua.cslab.asap.rest.beans.OperatorDescription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Map.Entry;

public class AbstractOperator implements Comparable<AbstractOperator> {
	public SpecTree optree;
	public String opName;
	
	public AbstractOperator(String name) {
		optree = new SpecTree();
		opName = name;
	}

	public void add(String key, String value) {
		optree.add(key,value);
	}
	


	public void moveOperator(Dataset from, Dataset to) {
		optree.addAll(from.datasetTree.copyInputToOpSubTree("Constraints","Input0"));
		optree.addAll(to.datasetTree.copyInputToOpSubTree("Constraints","Output0"));
		optree.add("Constraints.OpSpecification.Algorithm.name", "move");
	}
	

	public void setOutput(SpecTree t, int i) {
	}

	public void setInput(SpecTree t, int i) {
		// TODO Auto-generated method stub
		
	}

	public String toKeyValues(String separator) {
		String ret ="";
		ret+=optree.toKeyValues("", ret,separator);
		return ret;
	}
	
	@Override
	public String toString() {
		String ret = opName+": ";
		ret+= optree.toString();
		return ret;
	}

	public boolean checkMatch(Operator op) {
		return optree.checkMatch(op.optree);
	}

	public void addRegex(NodeName key, String value) {
		optree.addRegex(key,value);
	}

	@Override
	public int compareTo(AbstractOperator o) {
		return opName.compareTo(o.opName);
	}

	public void writeToPropertiesFile(String filename) throws IOException {
        Properties props = new Properties();

		optree.writeToPropertiesFile("", props);
        File f = new File(filename);
        if (!f.exists()) {
        	f.createNewFile();
        }
        OutputStream out = new FileOutputStream( f );
        props.store(out,"");
        out.close();
	}

	public void readPropertiesFromFile(File file) throws IOException{
		InputStream stream = new FileInputStream(file);
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
		stream.close();
	}

	public void readPropertiesFromFile(InputStream stream) throws IOException{
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
		stream.close();
	}

	public OperatorDescription toOperatorDescription() {
		OperatorDescription ret = new OperatorDescription(opName, "");
		optree.toOperatorDescription(ret);
		return ret;
	}
	
	
}
