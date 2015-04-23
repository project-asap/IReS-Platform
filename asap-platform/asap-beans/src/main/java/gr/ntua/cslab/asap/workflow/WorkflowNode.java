package gr.ntua.cslab.asap.workflow;

import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.apache.log4j.Logger;

import weka.core.Attribute;

public class WorkflowNode implements Comparable<WorkflowNode>{
	private boolean visited;
	private Double optimalCost;
	public boolean isOperator,isAbstract;
	public Operator operator;
	public AbstractOperator abstractOperator;
	public Dataset dataset;
	public List<WorkflowNode> inputs;
	private static Logger logger = Logger.getLogger(WorkflowNode.class.getName());
	public boolean copyToLocal=false, copyToHDFS=false;
	
	public WorkflowNode(boolean isOperator, boolean isAbstract) {
		this.isOperator = isOperator;
		this.isAbstract = isAbstract;
		inputs = new ArrayList<WorkflowNode>();
		visited=false;
		optimalCost=0.0;
	} 
	
	public void setOperator(Operator operator){
		this.operator=operator;
	}
	
	public void setAbstractOperator(AbstractOperator abstractOperator){
		this.abstractOperator=abstractOperator;
	}
	
	public void setDataset(Dataset dataset){
		this.dataset=dataset;
	}
	
	public void addInput(WorkflowNode input){
		inputs.add(input);
	}

	public void addInputs(List<WorkflowNode> inputs){
		this.inputs.addAll(inputs);
	}


	public List<WorkflowNode> materialize(String metric, MaterializedWorkflow1 materializedWorkflow, Workflow1DPTable dpTable) throws Exception {
		
		logger.info("Processing : "+toStringNorecursive());
		List<WorkflowNode> ret = new ArrayList<WorkflowNode>();
		List<List<WorkflowNode>> materializedInputs = new ArrayList<List<WorkflowNode>>();
		for(WorkflowNode in : inputs){
			List<WorkflowNode> l = in.materialize(metric, materializedWorkflow,dpTable);
			materializedInputs.add(l);
		}
		logger.info(materializedInputs);
		if(isOperator){
			if(isAbstract){
				List<Operator> operators = OperatorLibrary.getMatches(abstractOperator);
				for(Operator op : operators){
					List<HashMap<String,Double>> minCostsForInput = new ArrayList<HashMap<String,Double>>();
					//Double operatorInputCost= 0.0;
					List<WorkflowNode> plan = new ArrayList<WorkflowNode>();
					logger.info("Materialized operator: "+op.opName);
					WorkflowNode temp = new WorkflowNode(true, false);
					temp.setOperator(op);
					int inputs = Integer.parseInt(op.getParameter("Constraints.Input.number"));
					boolean inputsMatch=true;
					List<WorkflowNode> bestInputs = new ArrayList<WorkflowNode>();
					for (int i = 0; i < inputs; i++) {
						Dataset tempInput = new Dataset("t"+materializedWorkflow.count);
						materializedWorkflow.count++;
						tempInput.inputFor(op,i);
						WorkflowNode tempInputNode = new WorkflowNode(false, false);
						tempInputNode.setDataset(tempInput);
						temp.addInput(tempInputNode);
						

						boolean inputMatches=false;
						Double operatorOneInputCost=0.0;
						if(materializedWorkflow.functionTarget.contains("min")){
							operatorOneInputCost= Double.MAX_VALUE;
						}
						else if(materializedWorkflow.functionTarget.contains("max")){
							operatorOneInputCost = -Double.MAX_VALUE;
						}
						HashMap<String,Double> oneInputMetrics = null;
						WorkflowNode bestInput = null;
						for(WorkflowNode in : materializedInputs.get(i)){
							logger.info("Checking: "+in.dataset.datasetName);
							if(tempInput.checkMatch(in.dataset)){
								logger.info("true");
								inputMatches=true;
								tempInputNode.addInput(in);
								if(materializedWorkflow.functionTarget.contains("min") && dpTable.getCost(in.dataset)<=operatorOneInputCost){
									operatorOneInputCost=dpTable.getCost(in.dataset);
									oneInputMetrics = dpTable.getMetrics(in.dataset);
									bestInput = in;
								}
								if(materializedWorkflow.functionTarget.contains("max") && dpTable.getCost(in.dataset)>=operatorOneInputCost){
									operatorOneInputCost=dpTable.getCost(in.dataset);
									oneInputMetrics = dpTable.getMetrics(in.dataset);
									bestInput = in;
								}
							}
							else{
								//check move
								//hdfs-local move
								/*WorkflowNode moveNoOp = new WorkflowNode(false, false);
								moveNoOp.inputs.add(in);
								Dataset temp2 = tempInput.clone();
								moveNoOp.setDataset(tempInput);
								String fs = temp2.getParameter("Constraints.Input"+i+".Engine.FS");
								if(fs.equals("local")){
									
								}*/
								
								//generic move
								logger.info("Check move ");
								List<Operator> moveOps = OperatorLibrary.checkMove(in.dataset, tempInput);
								if(!moveOps.isEmpty()){
									logger.info("true");
									inputMatches=true;
									for(Operator m : moveOps){
										WorkflowNode moveNode = new WorkflowNode(true, false);
										moveNode.setOperator(m);
										moveNode.addInput(in);
										tempInputNode.addInput(moveNode);
										moveNode.setOptimalCost(m.getMettric(metric, moveNode.inputs));
										Double tempCost = dpTable.getCost(in.dataset)+moveNode.getCost();
										
										if(materializedWorkflow.functionTarget.contains("min") && tempCost<=operatorOneInputCost){
											operatorOneInputCost=tempCost;
											HashMap<String, Double> prevMetrics = dpTable.getMetrics(in.dataset);
											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : prevMetrics.entrySet()){
												oneInputMetrics.put(e.getKey(), e.getValue()+m.getMettric(e.getKey(), moveNode.inputs));
											}
											bestInput=moveNode;
										}

										if(materializedWorkflow.functionTarget.contains("max") && tempCost>=operatorOneInputCost){
											operatorOneInputCost=tempCost;
											HashMap<String, Double> prevMetrics = dpTable.getMetrics(in.dataset);
											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : prevMetrics.entrySet()){
												oneInputMetrics.put(e.getKey(), e.getValue()+m.getMettric(e.getKey(), moveNode.inputs));
											}
											bestInput=moveNode;
										}
									}
									
								}

								
							}
						}
						if(!inputMatches){
							inputsMatch=false;
							break;
						}
						//System.out.println(materializedInputs.get(i)+"fb");
						//tempInputNode.addInputs(materializedInputs.get(i));
						minCostsForInput.add(oneInputMetrics);
						//System.out.println(bestInput+ "cost: "+operatorOneInputCost);
						/*if(operatorOneInputCost>operatorInputCost){
							operatorInputCost=operatorOneInputCost;
						}*/
						bestInputs.add(bestInput);
						if(bestInput.isOperator){
							//move
							plan.addAll(dpTable.getPlan(bestInput.inputs.get(0).dataset));
							plan.add(bestInput);
						}
						else{
							plan.addAll(dpTable.getPlan(bestInput.dataset));
						}
						plan.add(tempInputNode);
					}
					if(inputsMatch){
						logger.info("all inputs match");
						int i =0;
						for(WorkflowNode bin : bestInputs){
							WorkflowNode tin = temp.inputs.get(i);
							System.out.println("copy path from: "+bin.getName()+" to "+tin.getName());
							if(bin.isOperator){
								//move
								bin.operator.copyExecPath(tin.dataset,0);
							}
							else{
								bin.dataset.copyExecPath(tin.dataset,0);
								bin.dataset.copyOptimization(tin.dataset);
								
							}
							i++;
						}
						
						WorkflowNode tempOutputNode = new WorkflowNode(false, false);
						Dataset tempOutput = new Dataset("t"+materializedWorkflow.count);
						materializedWorkflow.count++;
						op.outputFor(tempOutput, 0, temp.inputs);
						//tempOutput.outputFor(op, 0, temp.inputs);
						tempOutputNode.setDataset(tempOutput);
						tempOutputNode.addInput(temp);
						ret.add(tempOutputNode);
						
						temp.setOptimalCost(op.getMettric(metric, bestInputs));
						plan.add(temp);
						plan.add(tempOutputNode);
						
						HashMap<String,Double> nextMetrics = new HashMap<String, Double>();
						for(String m : minCostsForInput.get(0).keySet()){
							List<Double> t1 = new ArrayList<Double>();
							for(HashMap<String, Double> h : minCostsForInput){
								t1.add(h.get(m));
							}
							Collections.sort(t1);
							//System.out.println(m+": "+t1);
							//System.out.println(minCostsForInput);
							String g = materializedWorkflow.groupInputs.get(m);
							//System.out.println(g);
							Double operatorInputCost=0.0;
							if(g.contains("min")){
								operatorInputCost=t1.get(0);
							}
							else if(g.contains("max")){
								operatorInputCost=t1.get(t1.size()-1);
							}
							else if(g.contains("sum")){
								for(Double d : t1){
									operatorInputCost+=d;
								}
							}
							nextMetrics.put(m, operatorInputCost+op.getMettric(m, bestInputs));
						}
						//System.out.println(nextMetrics);
						dpTable.addRecord(tempOutput, plan, computePolicyFunction(nextMetrics, materializedWorkflow.function), nextMetrics);
					}
				}
			}
			else{
				
			}
		}
		else{
			if(isAbstract){

				/*WorkflowNode temp = new WorkflowNode(false, false);
				temp.setDataset(dataset);
				for(List<WorkflowNode> l : materializedInputs){
					temp.addInputs(l);
				}
				ret.add(temp);*/
				for(List<WorkflowNode> l : materializedInputs){
					ret.addAll(l);
				}
			}
			else{
				WorkflowNode temp = new WorkflowNode(false, false);
				temp.setDataset(dataset);
				for(List<WorkflowNode> l : materializedInputs){
					temp.addInputs(l);
				}
				ret.add(temp);
				
				List<WorkflowNode> plan = new ArrayList<WorkflowNode>();
				plan.add(temp);
				HashMap<String,Double> metrics = new HashMap<String, Double>();
				for(String m : materializedWorkflow.groupInputs.keySet()){
					metrics.put(m, 0.0);
				}
				
				dpTable.addRecord(dataset, plan, computePolicyFunction(metrics, materializedWorkflow.function),metrics);
				
			}			
		}
		//System.out.println("Finished : "+toStringNorecursive());
		return ret;
	}
	
	protected Double computePolicyFunction(HashMap<String,Double> metrics, String function) throws NumberFormatException, EvaluationException {
		//System.out.println("Computing function "+ metrics);

		Evaluator evaluator = new Evaluator();
		Double res=0.0;
		String tempFunction = new String(function);
		for(String m : metrics.keySet()){
			tempFunction=tempFunction.replace(m, metrics.get(m)+"");
		}
    	res = Double.parseDouble(evaluator.evaluate(tempFunction));
		//System.out.println(res);
		return res;
	}
	
	@Override
	public int compareTo(WorkflowNode o) {
		if(this.isOperator != o.isOperator){
			if(this.isOperator)
				return -1;
			else
				return 1;
		}
		else{
			if(this.isOperator){
				if(this.isAbstract!=o.isAbstract)
					return -1;
				else if (this.isAbstract)
					return this.abstractOperator.opName.compareTo(o.abstractOperator.opName);
				else
					return this.operator.opName.compareTo(o.operator.opName);
			}
			else
				return this.dataset.compareTo(o.dataset);
		}
	}

	public String toStringNorecursive() {
		String ret = "";
		if(isOperator){
			if(isAbstract)
				ret+=abstractOperator.opName;
			else
				ret+=operator.opName;
		}
		else{
			ret+=dataset.datasetName;
		}
		return ret;
	}

	public String getName() {
		String ret = "";
		if(isOperator){
			if(isAbstract)
				ret+=abstractOperator.opName;
			else
				ret+=operator.opName;
		}
		else{
			ret+=dataset.datasetName;
		}
		return ret;
	}
	
	public String toStringRecursive() {
		String ret = "";
		if(isOperator){
			if(isAbstract)
				ret+=abstractOperator.opName;
			else
				ret+=operator.opName;
		}
		else{
			ret+=dataset.datasetName;
		}
		if(inputs.size()>0){
			ret+=" { ";
			int i=0;
			for(WorkflowNode n : inputs){
				if(i!=0)
					ret+=", ";
				ret+=n.toStringRecursive();
				i++;
			}
			ret+=" }";
		}
		return ret;
	}
	
	@Override
	public String toString() {
		String ret = "";
		if(isOperator){
			if(isAbstract)
				ret+=abstractOperator.opName;
			else
				ret+=operator.opName;
		}
		else{
			ret+=dataset.datasetName;
		}
		/*if(inputs.size()>0){
			ret+=" { ";
			int i=0;
			for(WorkflowNode n : inputs){
				if(i!=0)
					ret+=", ";
				ret+=n.toString();
				i++;
			}
			ret+=" }";
		}*/
		return ret;
	}

	public void printNodes() {
		if(!visited){
			for(WorkflowNode n : inputs){
				System.out.println(n.toStringNorecursive() +"->"+toStringNorecursive());
			}
			for(WorkflowNode n : inputs){
				n.printNodes();
			}
			visited=true;
		}
	}

	public Double getCost() throws NumberFormatException, EvaluationException{
		if(isOperator && !isAbstract){
    		return optimalCost;
		}
		else{
    		return 0.0;
		}
	}
	
	public void setOptimalCost(Double optimalCost) {
		this.optimalCost = optimalCost;
	}
	
	public String getStatus(HashMap<String, List<WorkflowNode>> bestPlans){
		logger.info("Check :"+toStringNorecursive());
		boolean found=false;
		for(List<WorkflowNode> l :bestPlans.values()){
			for(WorkflowNode n : l){
				if(n.toStringNorecursive().equals(toStringNorecursive())){
					found=true;
					break;
				}
			}
			if(found)
				break;
		}
		if(found){
			logger.info("running");
			return "running";
		}
		else{
			logger.info("stopped");
			return "stopped";
		}
	}
	
	public void toWorkflowDictionary(WorkflowDictionary ret, HashMap<String, List<WorkflowNode>> bestPlans, String delimiter, List<WorkflowNode> targets) throws NumberFormatException, EvaluationException {
		if(!visited){
			OperatorDictionary op= new OperatorDictionary(toStringNorecursive(), String.format( "%.2f", getCost() ), 
					getStatus(bestPlans), isOperator+"", toKeyValueString(delimiter), targets.contains(this));
			
			for(WorkflowNode n : inputs){
				op.addInput(n.toStringNorecursive());
				n.toWorkflowDictionary(ret, bestPlans, delimiter, targets);
			}
	    	ret.addOperator(op);
			visited=true;
		}
		
	}
	
	public String toKeyValueString(String delimiter) {
		if(isOperator){
			if(isAbstract){
				return abstractOperator.toKeyValues(delimiter);
			}
			else{
				return operator.toKeyValues(delimiter);
			}
		}
		else{
			return dataset.toKeyValues(delimiter);
		}
	}

	public void writeToDir(String opDir, String datasetDir,BufferedWriter graphWritter) throws Exception {

		if(!visited){

			if(isOperator){
				if(isAbstract){
					abstractOperator.writeToPropertiesFile(opDir+"/"+abstractOperator.opName);
				}
				else{
					operator.directory=opDir+"/"+operator.opName;
					operator.writeToPropertiesFile(opDir+"/"+operator.opName);
				}
			}
			else{
				dataset.writeToPropertiesFile(datasetDir+"/"+dataset.datasetName);
			}
			for(WorkflowNode n : inputs){
				graphWritter.write(n.toStringNorecursive() +","+toStringNorecursive());
				graphWritter.newLine();
			}
			for(WorkflowNode n : inputs){
				n.writeToDir(opDir, datasetDir, graphWritter);
			}
			visited=true;
		}
	}
	
	public void graphToString(BufferedWriter graphWritter) throws IOException {
		for(WorkflowNode n : inputs){
			graphWritter.write(n.toStringNorecursive() +","+toStringNorecursive());
			graphWritter.newLine();
		}
	}

	public void graphToStringRecursive(BufferedWriter graphWritter) throws IOException {

		if(!visited){
			for(WorkflowNode n : inputs){
				graphWritter.write(n.toStringNorecursive() +","+toStringNorecursive());
				graphWritter.newLine();
			}
			for(WorkflowNode n : inputs){
				n.graphToString(graphWritter);
			}
			visited=true;
		}
	}

	public void setAllNotVisited() {
		visited=false;
		for(WorkflowNode n : inputs){
			n.setAllNotVisited();
		}
	}

	public String getArguments() {
		if(!isOperator)
			return "";
		else{
			String ret = "";
		    for (int i = 0; i < Integer.parseInt(operator.getParameter("Execution.Arguments.number")); i++) {
		    	String arg = operator.getParameter("Execution.Argument"+i);
		    	if(arg.startsWith("In")){
		    		int index = Integer.parseInt(arg.charAt(2)+"");
		    		WorkflowNode n = inputs.get(index);
		    		String parameter =arg.substring(arg.indexOf(".")+1);
		    		if(parameter.endsWith("local")){
		    			parameter=parameter.replace(".local", "");
		    			logger.info("parameter: "+parameter);
		    			
		    			String newArg = n.dataset.getParameter("Execution."+parameter);
		    			logger.info("newArg: "+newArg);
		    			newArg = newArg.substring(newArg.lastIndexOf("/")+1, newArg.length());
		    			logger.info("local path: "+newArg);
			    		arg=newArg;
		    		}
		    		else{
			    		String newArg = n.dataset.getParameter("Execution."+parameter);
			    		logger.info("newArg: "+newArg);
			    		arg=newArg;
		    		}
		    		/*boolean dataset = false;
		    		while(!n.isOperator){
		    			if(n.inputs.isEmpty()){
		    				arg = n.dataset.datasetName;
		    				dataset=true;
		    				break;
		    			}
		    			else{
		    				n=n.inputs.get(0);
		    			}
		    		}
		    		if(!dataset)
		    			arg = n.operator.getParameter("Execution.Output0.path");*/
		    	}
		    	ret+= arg+" ";
			}
			return ret;
		}
	}
	public List<String> getOutputFiles() {
		List<String> ret = new ArrayList<String>();
		if(!isOperator)
			return ret;
		else{
			String outFiles = operator.getParameter("Execution.copyFromLocal");
			if(outFiles==null)
				return ret;
			String[] files = outFiles.split(",");
			for (int i = 0; i < files.length; i++) {
			    ret.add(files[i]);
			}
			return ret;
		}
	}
	
	public HashMap<String, String> getInputFiles() {
		HashMap<String, String> ret = new HashMap<String, String>();
		if(!isOperator)
			return ret;
		else{
			String inFiles = operator.getParameter("Execution.copyToLocal");
			if(inFiles==null)
				return ret;
			String[] files = inFiles.split(",");
			for (int i = 0; i < files.length; i++) {
				if(files[i].startsWith("In")){
					int index = Integer.parseInt(files[i].charAt(2)+"");
		    		WorkflowNode n = inputs.get(index);
					String path = n.dataset.getParameter("Execution.path");
			    	ret.put(path.substring(path.lastIndexOf("/")+1),path);
				}
				else{
			    	ret.put(files[i].substring(files[i].lastIndexOf("/")+1),files[i]);
				}
			}
			/*for(WorkflowNode in : inputs){
				String path = in.dataset.getParameter("Execution.path");

		    	ret.put(path.substring(path.lastIndexOf("/")+1),path);
			}*/
		    /*for (int i = 0; i < Integer.parseInt(operator.getParameter("Execution.Arguments.number"))-1; i++) {
		    	String arg = operator.getParameter("Execution.Argument"+i);
		    	String operatorName = "";
		    	if(arg.startsWith("In")){
		    		int index = Integer.parseInt(arg.charAt(2)+"");
		    		WorkflowNode n = inputs.get(index);
		    		boolean dataset = false;
		    		while(!n.isOperator){
		    			if(n.inputs.isEmpty()){
		    				arg = n.dataset.datasetName;
		    				dataset=true;
		    				break;
		    			}
		    			else{
		    				n=n.inputs.get(0);
		    			}
		    		}
		    		if(!dataset){
		    			arg = n.operator.getParameter("Execution.Output0.fileName");
		    			operatorName= n.operator.opName;
		    		}
		    	}
		    	ret.put(arg,operatorName);
			}*/
			return ret;
		}
	}

	private boolean isDataset() {
		return !isOperator;
	}
}

