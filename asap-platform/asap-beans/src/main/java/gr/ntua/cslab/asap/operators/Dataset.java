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
import gr.ntua.cslab.asap.workflow.WorkflowNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class Dataset implements Comparable<Dataset> {


	public SpecTree datasetTree;
	public String datasetName;
	private static Logger logger = Logger.getLogger(Dataset.class.getName());
	
	public Dataset(String name) {
		datasetTree = new SpecTree();
		datasetName = name;
	}

	@Override
	public Dataset clone() throws CloneNotSupportedException {
		Dataset ret = new Dataset(datasetName);
		ret.datasetTree = datasetTree.clone();
		return ret;
	}
	
	public void add(String key, String value) {
		datasetTree.add(key,value);
	}
	
	@Override
	public String toString() {
		String ret = datasetName+": ";
		ret+= datasetTree.toString();
		return ret;
	}

	@Override
	public int compareTo(Dataset o) {
		return datasetName.compareTo(o.datasetName);
	}

	public void inputFor(Operator op, int position) {
		datasetTree = op.optree.copyInputSubTree("Constraints.Input"+position);
		if(datasetTree == null)
			datasetTree = new SpecTree();
	}

	public boolean checkMatch(Dataset d) {
		//logger.info("Checking match: "+ this.toString()+"  -  "+d );
		return datasetTree.checkMatch(d.datasetTree);
	}


	
	public void outputFor(Operator op, int position) {
		//System.out.println("Generating output for pos: "+ position);
		datasetTree = op.optree.copyInputSubTree("Constraints.Output"+position);
		if(datasetTree == null)
			datasetTree = new SpecTree();
	}

	public void writeToPropertiesFile(String filename) throws IOException {
        Properties props = new Properties();

        datasetTree.writeToPropertiesFile("", props);
        File f = new File(filename);
        if (!f.exists()) {
        	f.createNewFile();
        }
        OutputStream out = new FileOutputStream( f );
        props.store(out,"");
        out.close();
	}
	

	public void readPropertiesFromFile(String filename) throws IOException{
        File f = new File(filename);
		InputStream stream = new FileInputStream(f);
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
		stream.close();
	}

	public void readPropertiesFromString(String properties) throws IOException {
		InputStream stream = new ByteArrayInputStream(properties.getBytes());
		readPropertiesFromFile(stream);
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
	public void readPropertiesFromFile(File file) throws IOException{
		InputStream stream = new FileInputStream(file);
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
		stream.close();
	}
	
	public String toKeyValues(String separator) {
		String ret ="";
		ret+=datasetTree.toKeyValues("", ret, separator);
		return ret;
	}

	public String getParameter(String key) {
		return datasetTree.getParameter(key);
	}

	public OperatorDescription toOperatorDescription() {
		OperatorDescription ret = new OperatorDescription(datasetName, "");
		datasetTree.toOperatorDescription(ret);
		return ret;
	}

	
	public void copyExecVariables(Dataset dataset, int position) {
		logger.info( "DATASET TREE: " + datasetTree);
		SpecTreeNode variables = datasetTree.getNode("Execution");
		HashMap<String, String> val = new HashMap<String, String>();
		variables.toKeyValues("", val);
		for(Entry<String, String> e: val.entrySet()){
			dataset.add("Execution."+e.getKey(), e.getValue());
		}
	}
	
	public void copyOptimization(Dataset dataset) {
		SpecTreeNode opt = datasetTree.getNode("Optimization");
		HashMap<String, String> kvs = new HashMap<String, String>();
		if(opt!=null){
			opt.toKeyValues("Optimization.", kvs);
			for(Entry<String, String> kv:kvs.entrySet())
				dataset.add(kv.getKey(), kv.getValue());
		}
	}

}
