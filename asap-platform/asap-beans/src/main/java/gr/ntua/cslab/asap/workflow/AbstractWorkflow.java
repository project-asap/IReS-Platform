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

import gr.ntua.cslab.asap.operators.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sourceforge.jeval.EvaluationException;

public class AbstractWorkflow {
	public HashMap<Dataset,List<AbstractOperator>> datasetDag, reverseDatasetDag;
	public HashMap<AbstractOperator,List<Dataset>> operatorDag, reverseOperatorDag;
	public List<Dataset> materializedDatasets;
	private MaterializedOperators library;
	private int count;
	
	public AbstractWorkflow(MaterializedOperators library) {
		this.library = library;
		datasetDag = new HashMap<Dataset, List<AbstractOperator>>();
		operatorDag = new HashMap<AbstractOperator, List<Dataset>>();
		reverseDatasetDag = new HashMap<Dataset, List<AbstractOperator>>();
		reverseOperatorDag = new HashMap<AbstractOperator, List<Dataset>>();
		materializedDatasets = new ArrayList<Dataset>();
	}


	public void addInputEdge(Dataset d, AbstractOperator abstractOperator, int inputPositon) {
		List<AbstractOperator> temp = datasetDag.get(d);
		if(temp==null){
			temp= new ArrayList<AbstractOperator>();
			datasetDag.put(d, temp);
		}
		temp.add(abstractOperator);
		
		List<Dataset> temp1 = reverseOperatorDag.get(abstractOperator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			reverseOperatorDag.put(abstractOperator, temp1);
		}
		temp1.add(inputPositon, d);
	}

	public void addOutputEdge(AbstractOperator abstractOperator, Dataset d, int outputPosition) {
		List<AbstractOperator> temp = reverseDatasetDag.get(d);
		if(temp==null){
			temp= new ArrayList<AbstractOperator>();
			reverseDatasetDag.put(d, temp);
		}
		temp.add(abstractOperator);
		
		List<Dataset> temp1 = operatorDag.get(abstractOperator);
		if(temp1==null){
			temp1= new ArrayList<Dataset>();
			operatorDag.put(abstractOperator, temp1);
		}
		temp1.add(outputPosition, d);
	}
	
	public void addMaterializedDatasets(List<Dataset> materializedDatasets) {
		this.materializedDatasets.addAll(materializedDatasets);		
	}
	
	@Override
	public String toString() {
		String ret ="Abstract Workflow: \n";
		for(Entry<AbstractOperator, List<Dataset>> e : operatorDag.entrySet()){
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
		return ret;
	}
	

	public Workflow getWorkflow(Dataset target) {
		Workflow ret = new Workflow();
		int count =0;
		for(AbstractOperator a : operatorDag.keySet()){
			System.out.println("check matches for: "+ a.opName);
			List<Operator> operators = library.getMatches(a);
			int i = 0;
			for(Dataset d : reverseOperatorDag.get(a)){
				System.out.println("check input: "+d.datasetName);
				for(Operator op : operators){
					System.out.println(op.opName);
					Dataset tempInput = new Dataset("t"+count);
					tempInput.inputFor(op,i);

					/*if(tempInput.checkMatch(e.getKey())){
						System.out.println("true");
						found=true;
						if(e.getValue()<min){
							min=e.getValue();
						}
					}*/
					
					
					System.out.println("Temp Input "+tempInput);
					ret.addInputEdgeOld(d, tempInput, op);
					count++;
				}
				i++;
			}
			for(Dataset d : operatorDag.get(a)){
				for(Operator op : operators){
					ret.addOutputEdgeOld(op, new Dataset("t"+count), d);
					count++;
				}
				
			}
		}
		
		return ret;
	}
	
	
	public void optimize(Dataset target, WorkflowDPTable dpTable) throws NumberFormatException, EvaluationException {
		if(dpTable.getCost(target)!=null){
			return;
		}
		if(materializedDatasets.contains(target)){
			dpTable.addRecord(target, target, new Workflow());
			return;
			//ret.put(target, new Double(0));
		}
		else{
			System.out.println("optimize: "+target.datasetName);
			List<AbstractOperator> l = reverseDatasetDag.get(target);
			int i = 0;
			for(AbstractOperator a : l){
				//find optimal materialized operator
				List<Operator> operators = library.getMatches(a);
				int jj =0;
				for(Operator op : operators){
					Workflow tempWorkflow = new Workflow();
					System.out.println("Operator: "+op.opName);
					Double maxCost = 0.0;
					int j = 0;
					boolean found0 = false;
					for(Dataset d : reverseOperatorDag.get(a)){
						optimize(d,dpTable);
						Double min = Double.MAX_VALUE;
						Dataset minDataset=null;
						Workflow minWorkflow=new Workflow();
						
						Dataset tempInput = new Dataset("t"+count);
						count++;
						tempInput.inputFor(op,j);
						//System.out.println("Dataset: "+d.datasetName);
						boolean found=false;
						for(Entry<Dataset, Workflow> e : dpTable.getCost(d).entrySet()){
							System.out.println("Checking: \n"+tempInput);
							System.out.println(e.getKey());
							minWorkflow.addEquivalence(e.getKey(), tempInput);
							minWorkflow.addAll(e.getValue());
							if(tempInput.checkMatch(e.getKey())){
								System.out.println("true");
								found=true;
								if(e.getValue().optimalCost<min){
									min=e.getValue().optimalCost;
									minDataset=e.getKey();
									//minWorkflow=e.getValue();
								}
							}
							else{
								//check move
								found=true;
								min=e.getValue().optimalCost;

								//if(e.getValue().optimalCost<min){
								
									minDataset=tempInput;
									minWorkflow=new Workflow();
									Operator m = new Operator("move","/tmp");
									minWorkflow.addInputEdge(e.getKey(), m, 0);
									minWorkflow.addOutputEdge(m, tempInput, 0);
								//}
								
							}
						}
						if(!found){
							//no much for this input 
							found0=true;
							maxCost=Double.MAX_VALUE;
							break;
						}
						if(min>maxCost){
							maxCost=min;
						}
						tempWorkflow.addInputEdge(minDataset, op, j);
						tempWorkflow.addAll(minWorkflow);
						j++;
							
					}
					if(found0){
						//at least one input not matching
						maxCost=Double.MAX_VALUE;
					}
					else{
						maxCost+=op.getCost(new ArrayList<WorkflowNode>());
						Dataset tempOutput = new Dataset("t"+count);
						count++;
						tempOutput.outputFor(op,0);
						tempWorkflow.addOutputEdge(op,tempOutput, 0);
						tempWorkflow.optimalCost=maxCost;
						jj++;
						dpTable.addRecord(target, tempOutput, tempWorkflow);
						//System.out.println(maxCost);
					}
				}
				
				/*if(i>0)
					System.out.print(",");
				System.out.print(a.opName+"(");
				int ii = 0;
				for(Dataset d : reverseOperatorDag.get(a)){
					if(ii>0)
						System.out.print(",");
					optimize(d);
					ii++;
				}
				System.out.print(")");
				i++;*/
			}
		}
		
	}
	
	public Workflow optimizeWorkflow(Dataset target) throws NumberFormatException, EvaluationException {
		Workflow ret = new Workflow();
		count =0;
		WorkflowDPTable dpTable = new WorkflowDPTable();
		optimize(target,dpTable);
		Double minCost= Double.MAX_VALUE;
		for( Entry<Dataset, Workflow> e : dpTable.getCost(target).entrySet() ){
			if(e.getValue().optimalCost<minCost){
				minCost = e.getValue().optimalCost;
			}
			ret.addAll(e.getValue());
		}
		System.out.println("minCost: "+minCost);
		/*Workflow ret = new Workflow();
		Double optCost = Double.MAX_VALUE;
		Dataset optDataset =null;
		for(Entry<Dataset, Double> e : m.entrySet()){
			if(optCost>e.getValue()){
				optCost = e.getValue();
				optDataset = e.getKey();
			}
		}
		ret.optimalCost=optCost;
		System.out.println(optDataset.datasetName+" cost: "+optCost);*/
		return ret;
	}
	


	public void writeToDir(String directory) throws IOException {
        File workflowDir = new File(directory);
        if (!workflowDir.exists()) {
        	workflowDir.mkdirs();
        }
        File opDir = new File(directory+"/operators");
        if (!opDir.exists()) {
        	opDir.mkdirs();
        }
        for(AbstractOperator op : operatorDag.keySet()){
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

		for(Entry<AbstractOperator, List<Dataset>> e : operatorDag.entrySet()){
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
    	bw.close();
        
        
	}
	
	public static void main(String[] args) throws Exception {
		MaterializedOperators library =  new MaterializedOperators();
		AbstractWorkflow abstractWorkflow = new AbstractWorkflow(library);
		Dataset d1 = new Dataset("hbaseDataset");
		d1.add("Constraints.DataInfo.Attributes.number","2");
		d1.add("Constraints.DataInfo.Attributes.Atr1.type","ByteWritable");
		d1.add("Constraints.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.key","Atr1");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.value","Atr2");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		d1.add("Optimization.size","1TB");
		d1.add("Optimization.uniqueKeys","1.3 billion");

		//d1.writeToPropertiesFile(d1.datasetName);
		
		Dataset d2 = new Dataset("mySQLDataset");
		d2.add("Constraints.DataInfo.Attributes.number","2");
		d2.add("Constraints.DataInfo.Attributes.Atr1.type","Varchar");
		d2.add("Constraints.DataInfo.Attributes.Atr2.type","Varchar");
		d2.add("Constraints.Engine.DB.Relational.MySQL.schema","...");
		d2.add("Constraints.Engine.DB.Relational.MySQL.location","127.0.0.1");
		d2.add("Optimization.size","1GB");
		d2.add("Optimization.uniqueKeys","1 million");
		
		//d2.writeToPropertiesFile(d2.datasetName);

		AbstractOperator abstractOp = new AbstractOperator("JoinOp");
		abstractOp.add("Constraints.Input.number","2");
		abstractOp.add("Constraints.Output.number","1");
		abstractOp.add("Constraints.Input0.DataInfo.Attributes.number","2");
		abstractOp.add("Constraints.Input1.DataInfo.Attributes.number","2");
		abstractOp.add("Constraints.Output0.DataInfo.Attributes.number","2");
		abstractOp.addRegex(new NodeName("Constraints.OpSpecification.Algorithm.Join", new NodeName(".*", null, true), false), ".*");

		//abstractOp.writeToPropertiesFile(abstractOp.opName);

		AbstractOperator abstractOp1 = new AbstractOperator("SortOp");
		abstractOp1.add("Constraints.Input.number","1");
		abstractOp1.add("Constraints.Output.number","1");
		abstractOp1.add("Constraints.Input0.DataInfo.Attributes.number","2");
		abstractOp1.add("Constraints.Output0.DataInfo.Attributes.number","2");
		abstractOp1.addRegex(new NodeName("Constraints.OpSpecification.Algorithm.Sort", new NodeName(".*", null, true), false), ".*");

		//abstractOp1.writeToPropertiesFile(abstractOp1.opName);
		
		Dataset d3 = new Dataset("d3");
		Dataset d4 = new Dataset("d4");
		abstractWorkflow.addInputEdge(d1,abstractOp,0);
		abstractWorkflow.addInputEdge(d2,abstractOp,1);
		abstractWorkflow.addOutputEdge(abstractOp,d3,0);
		abstractWorkflow.addInputEdge(d3,abstractOp1,0);
		abstractWorkflow.addOutputEdge(abstractOp1,d4,0);
		List<Dataset> materializedDatasets = new ArrayList<Dataset>();
		materializedDatasets.add(d1);
		materializedDatasets.add(d2);
		abstractWorkflow.addMaterializedDatasets(materializedDatasets);
		System.out.println(abstractWorkflow);
		
		
		abstractWorkflow.writeToDir("asapLibrary/workflows/workflow1");
		
		
		
		//Workflow workflow = abstractWorkflow.getWorkflow(d4);
		//workflow.writeToDir("asapLibrary/workflows/matwork");
		
		//System.out.print(d4.datasetName+"=");
		Workflow workflow1 = abstractWorkflow.optimizeWorkflow(d4);
		System.out.println(workflow1);
		workflow1.writeToDir("asapLibrary/workflows/matwork");
		//System.out.println();
		//System.out.println(workflow1);
	}









}
