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


package gr.ntua.cslab.asap.workflow;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.operators.Dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Workflow {
	public HashMap<Dataset,List<Operator>> datasetDag, reverseDatasetDag;
	public HashMap<Operator,List<Dataset>> operatorDag, reverseOperatorDag;
	public HashMap<Dataset,List<Dataset>> equivalentDatasets;
	public double optimalCost;
	
	public Workflow() {
		datasetDag = new HashMap<Dataset, List<Operator>>();
		operatorDag = new HashMap<Operator, List<Dataset>>();
		reverseDatasetDag = new HashMap<Dataset, List<Operator>>();
		reverseOperatorDag = new HashMap<Operator, List<Dataset>>();
		equivalentDatasets = new HashMap<Dataset, List<Dataset>>();
		optimalCost =0.0;
	}
	
	public void addInputEdgeOld(Dataset d, Dataset tempDataset, Operator operator) {
		List<Operator> temp = datasetDag.get(tempDataset);
		if(temp==null){
			temp= new ArrayList<Operator>();
			datasetDag.put(tempDataset, temp);
		}
		temp.add(operator);
		
		List<Dataset> temp1 = reverseOperatorDag.get(operator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			reverseOperatorDag.put(operator, temp1);
		}
		temp1.add(tempDataset);
		
		temp1 = equivalentDatasets.get(d);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			equivalentDatasets.put(d, temp1);
		}
		temp1.add(tempDataset);
	}

	public void addOutputEdgeOld(Operator operator, Dataset tempDataset, Dataset d) {
		List<Operator> temp = reverseDatasetDag.get(tempDataset);
		if(temp==null){
			temp= new ArrayList<Operator>();
			reverseDatasetDag.put(tempDataset, temp);
		}
		temp.add(operator);
		
		List<Dataset> temp1 = operatorDag.get(operator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			operatorDag.put(operator, temp1);
		}
		temp1.add(tempDataset);
		
		temp1 = equivalentDatasets.get(d);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			equivalentDatasets.put(d, temp1);
		}
		temp1.add(tempDataset);
	}
	
	public void addEquivalence(Dataset src, Dataset dst){

		List<Dataset> temp = equivalentDatasets.get(src);
		if(temp==null){
			temp= new ArrayList<Dataset>();
			equivalentDatasets.put(src, temp);
		}
		temp.add(dst);
	}
	
	public void addInputEdge(Dataset d, Operator operator, int inputPositon) {
		System.out.println("Adding edge :"+d.datasetName+" -> "+operator.opName+" pos: "+inputPositon);
		List<Operator> temp = datasetDag.get(d);
		if(temp==null){
			temp= new ArrayList<Operator>();
			datasetDag.put(d, temp);
		}
		temp.add(operator);
		
		List<Dataset> temp1 = reverseOperatorDag.get(operator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			reverseOperatorDag.put(operator, temp1);
		}
		temp1.add(inputPositon, d);
	}

	public void addOutputEdge(Operator operator, Dataset d, int outputPosition) {
		System.out.println("Adding edge :"+operator.opName+" -> "+d.datasetName+" pos: "+outputPosition);
		List<Operator> temp = reverseDatasetDag.get(d);
		if(temp==null){
			temp= new ArrayList<Operator>();
			reverseDatasetDag.put(d, temp);
		}
		temp.add(operator);
		
		List<Dataset> temp1 = operatorDag.get(operator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			operatorDag.put(operator, temp1);
		}
		temp1.add(outputPosition, d);
	}
	
	
	@Override
	public String toString() {String ret ="Workflow: \n";
		for(Entry<Operator, List<Dataset>> e : operatorDag.entrySet()){
			ret+=e.getKey().opName+" in:{";
			for(Dataset d : reverseOperatorDag.get(e.getKey())){
				ret+= d.datasetName+", ";
			}
			ret+="} out:{";
			for(Dataset edge : e.getValue()){
				ret+=edge.datasetName+", ";
			}
			ret+="}\n";
		}
		ret+="Equivalence: \n";
		for(Entry<Dataset, List<Dataset>> e : equivalentDatasets.entrySet()){
			ret+=e.getKey().datasetName+" eq: {";
			for(Dataset edge : e.getValue()){
				ret+=edge.datasetName+", ";
			}
			ret+="}\n";
		}
		return ret;
	}

	public void writeToDir(String directory) throws Exception {
        File workflowDir = new File(directory);
        if (!workflowDir.exists()) {
        	workflowDir.mkdirs();
        }
        File opDir = new File(directory+"/operators");
        if (!opDir.exists()) {
        	opDir.mkdirs();
        }
        for(Operator op : operatorDag.keySet()){
        	op.writeToPropertiesFile(directory+"/operators/"+op.opName);
        }
        
        File datasetDir = new File(directory+"/datasets");
        if (!datasetDir.exists()) {
        	datasetDir.mkdirs();
        }
        for(Dataset d : datasetDag.keySet()){
        	d.writeToPropertiesFile(directory+"/datasets/"+d.datasetName);
        }
        
        File edgeGraph = new File(directory+"/graph");
    	FileOutputStream fos = new FileOutputStream(edgeGraph);
    	 
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		for(Entry<Operator, List<Dataset>> e : operatorDag.entrySet()){
			int i=0;
			for(Dataset d : reverseOperatorDag.get(e.getKey())){
				bw.write("0,"+d.datasetName+","+e.getKey().opName+","+i);
	    		bw.newLine();
				i++;
			}
			i=0;
			for(Dataset edge : e.getValue()){
				bw.write("1,"+e.getKey().opName+","+edge.datasetName+","+i);
	    		bw.newLine();
				i++;
			}
		}
		for(Entry<Dataset, List<Dataset>> e : equivalentDatasets.entrySet()){
			int i=0;
			for(Dataset edge : e.getValue()){
				bw.write("0,"+e.getKey().datasetName+","+edge.datasetName+","+i);
	    		bw.newLine();
				i++;
			}
		}
    	bw.close();
        
        
	}

	public void addAll(Workflow o) {
		this.datasetDag.putAll(o.datasetDag);
		this.reverseDatasetDag.putAll(o.reverseDatasetDag);
		this.operatorDag.putAll(o.operatorDag);
		this.reverseOperatorDag.putAll(o.reverseOperatorDag);
		this.equivalentDatasets.putAll(o.equivalentDatasets);
	}
}
