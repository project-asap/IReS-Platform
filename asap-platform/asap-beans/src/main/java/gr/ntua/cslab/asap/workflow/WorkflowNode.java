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

import gr.ntua.cslab.asap.staticLibraries.ClusterStatusLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;

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
	private String abstractName;

	private boolean visited;
	private Double optimalCost,execTime;
	public boolean isOperator,isAbstract;
	public Operator operator;
	public AbstractOperator abstractOperator;
	public Dataset dataset;
	public List<WorkflowNode> inputs, outputs;
	private static Logger logger = Logger.getLogger(WorkflowNode.class.getName());
	public boolean copyToLocal=false, copyToHDFS=false;
	public String inMonitorValues;


	public WorkflowNode(boolean isOperator, boolean isAbstract, String abstractName) {
		this.abstractName = abstractName;
		this.isOperator = isOperator;
		this.isAbstract = isAbstract;
		inputs = new ArrayList<WorkflowNode>(10);
		outputs = new ArrayList<WorkflowNode>(10);
		visited=false;
		optimalCost=0.0;
		execTime=0.0;
	}

	public String getAbstractName() {
		return abstractName;
	}

	public void setAbstractName(String abstractName) {
		this.abstractName = abstractName;
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

	public void addOutput(WorkflowNode input){
		outputs.add(input);
	}

	public void addInput(int index, WorkflowNode input) {
		inputs.add(index,input);
	}

	public void addOutput(int index, WorkflowNode input) {
		outputs.add(index,input);
	}

	public void addInputs(List<WorkflowNode> inputs){
		this.inputs.addAll(inputs);
//		for(WorkflowNode in : inputs){
//			in.outputs.add(this);
//		}
	}


	public List<WorkflowNode> materialize(MaterializedWorkflow1 materializedWorkflow, Workflow1DPTable dpTable, String fromName) throws Exception {
		logger.info("Processing : " + toStringNorecursive()+" from name: "+fromName);
		//System.out.println("Processing : " + toStringNorecursive()+" from name: "+fromName);
		List<WorkflowNode> ret = new ArrayList<WorkflowNode>();
		List<List<WorkflowNode>> materializedInputs = new ArrayList<List<WorkflowNode>>();
		WorkflowNode temp = null;
		if(!isOperator){
			List<WorkflowNode> p = dpTable.getPlan(dataset);
			if(p!=null){
				ret.addAll(p);
				return ret;
			}
		}


		//check if intermediate results exist (replan)
		if( !isOperator){
			temp = materializedWorkflow.materilizedDatasets.get(getName());
			if(temp!=null){
				logger.info("Found existing dataset : " + toStringNorecursive());
				ret.add(temp);
				List<WorkflowNode> plan = new ArrayList<WorkflowNode>();
				plan.add(temp);
				HashMap<String,Double> metrics = new HashMap<String, Double>();
				for(String m : materializedWorkflow.groupInputs.keySet()){
					metrics.put(m, 0.0);
				}
				dpTable.addRecord(temp.dataset, plan, temp.computePolicyFunction(metrics, materializedWorkflow.function),metrics);
				return ret;
			}
		}

		for(WorkflowNode in : inputs){
			List<WorkflowNode> l = in.materialize(materializedWorkflow,dpTable,getName());
			materializedInputs.add(l);
		}
		logger.info( "Materialized inputs: " + materializedInputs);
		if(isOperator){
			if(isAbstract){
				List<Operator> operators = OperatorLibrary.getMatches(abstractOperator);
				//logger.info( "For abstract operator " + abstractOperator.opName + " the available"
				//			 + " operator implementations are:\n " + operators);
				for(Operator op : operators){
					if(!ClusterStatusLibrary.checkEngineStatus(op)){
						logger.info( "Specified engine for operator " + op.opName + " is " + op.getEngine());
						logger.info( "an it is not running. For this, this operator will no be materialized");
						logger.info( "and consequently the corresponding workflow will not be materialized.");
						continue;					
					}
					List<HashMap<String,Double>> minCostsForInput = new ArrayList<HashMap<String,Double>>();
					//Double operatorInputCost= 0.0;
					List<WorkflowNode> plan = new ArrayList<WorkflowNode>();
					logger.info("Materialized operator: " + op.opName);
					temp = new WorkflowNode(true, false,"");
					temp.setOperator(op);
					int inputs = Integer.parseInt(op.getParameter("Constraints.Input.number"));
					boolean inputsMatch=true;
					List<WorkflowNode> bestInputs = new ArrayList<WorkflowNode>();
					for (int i = 0; i < inputs; i++) {
						Dataset tempInput = new Dataset("t"+materializedWorkflow.count);
						materializedWorkflow.count++;
						tempInput.inputFor(op,i);
						WorkflowNode tempInputNode = new WorkflowNode(false, false,"");
						tempInputNode.setDataset(tempInput);
						temp.addInput(tempInputNode);

						boolean inputMatches=false;
						/* vpapa: if an input tx does not match with an operator it does not
							mean that another input ty may not match with the operator. For this,
							before calling a move operator try all other inputs. Keep track
							of the checked inputs with the variable below
						*/
						int checkedInputs = 0;
						Double operatorOneInputCost=0.0;
						if(materializedWorkflow.functionTarget.contains("min")){
							operatorOneInputCost= Double.MAX_VALUE;
						}
						else if(materializedWorkflow.functionTarget.contains("max")){
							operatorOneInputCost = -Double.MAX_VALUE;
						}
						HashMap<String,Double> oneInputMetrics = null;
						WorkflowNode bestInput = null;
						logger.info( "materializedInputs: " + materializedInputs);
						for(WorkflowNode in : materializedInputs.get(i)){
							logger.info("CHECKING INPUT DATASET: "+in.dataset.datasetName);
							/* vpapa: in case the property Constraints.Inputx.type
								is defined into an operator's description file for
								some input x( or all of them) but the property is not
								correspondingly defined into the input( dataset)
								description file, then an input mismatch will occur
								and the workflow materialization will fail and will
								not be displayed at IReS WUI. However IReS platform
								will still operate without giving any useful message.
								For this, precautiously, we write this event into
								the logs
							*/
							if( !tempInput.checkMatch(in.dataset)){
								logger.info( "ERROR: For operator " + op.opName + " there "
											+ " is an input mismatch. Check inside its"
											+ " description file if all properties Constraints.Input"
											+ " for some input x match with all the corresponding"
											+ " properties of the input dataset x, probably a"
											+ " materialized one, like the very first input( s)"
											+ " of the workflow. This message should be taken"
											+ " as a real error when the materialization seems"
											+ " to succeed when pushing 'Materialize Workflow'"
											+ " button but the workflow is not displayed at all.");
								logger.info( "Input dataset: " + in.dataset);
								logger.info( "Input to be matched: " + tempInput);
								//one input checked, go for the next
								logger.info( "checkedInputs: " + checkedInputs);
								logger.info( "materializedInputs.size(): " + materializedInputs.size());
								logger.info( "materializedInputs.get("+i+").size(): " + materializedInputs.get(i).size());								
								if( checkedInputs < materializedInputs.get( i).size()){
									checkedInputs++;
									logger.info( "checkedInputs: " + checkedInputs);
									continue;
								}
								else{
									//try for each input that does not match a move operator
									//i = 0;
								}
							}
							if( tempInput.checkMatch(in.dataset)){
								logger.info("true");
								inputMatches=true;
								tempInputNode.setAbstractName(in.getName());
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
								logger.info( "Move operators: " + moveOps);
								if(!moveOps.isEmpty()){
									logger.info("Are there any available move operators? True");
									inputMatches=true;
									for(Operator m : moveOps){
										WorkflowNode moveNode = new WorkflowNode(true, false,"");
										moveNode.setOperator(m);
										logger.info( "Move node added input:\n" + in);
										moveNode.addInput(in);
										List<WorkflowNode> lin= new ArrayList<WorkflowNode>();
										lin.add(in);
										tempInputNode.addInput(moveNode);
										HashMap<String, Double> prevMetrics = dpTable.getMetrics(in.dataset);
										Double prevCost = computePolicyFunction(prevMetrics, materializedWorkflow.function);
										HashMap<String,Double> nextMetrics = m.getOptimalPolicyCost(prevMetrics, lin, materializedWorkflow.function);

										m.generateOptimizationMetrics(tempInput, 0, nextMetrics);

										Double optCost = computePolicyFunction(nextMetrics, materializedWorkflow.function);
										moveNode.setOptimalCost(optCost-prevCost);

										moveNode.setExecTime(nextMetrics.get("execTime")-prevMetrics.get("execTime"));
										//moveNode.setOptimalCost(m.getMettric(metric, moveNode.inputs));
										Double tempCost = dpTable.getCost(in.dataset)+moveNode.getCost();

										if(materializedWorkflow.functionTarget.contains("min") && tempCost<=operatorOneInputCost){
											operatorOneInputCost=tempCost;
											/*HashMap<String, Double> prevMetrics = dpTable.getMetrics(in.dataset);

											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : prevMetrics.entrySet()){
												oneInputMetrics.put(e.getKey(), e.getValue()+m.getMettric(e.getKey(), moveNode.inputs));
											}*/
											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : nextMetrics.entrySet()){
												if(prevMetrics.containsKey(e.getKey())){
													oneInputMetrics.put(e.getKey(),e.getValue());
												}
									        }
											bestInput=moveNode;
										}

										if(materializedWorkflow.functionTarget.contains("max") && tempCost>=operatorOneInputCost){
											operatorOneInputCost=tempCost;
											/*HashMap<String, Double> prevMetrics = dpTable.getMetrics(in.dataset);
											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : prevMetrics.entrySet()){
												oneInputMetrics.put(e.getKey(), e.getValue()+m.getMettric(e.getKey(), moveNode.inputs));
											}*/
											oneInputMetrics = new HashMap<String, Double>();
											for(Entry<String, Double> e : nextMetrics.entrySet()){
												if(prevMetrics.containsKey(e.getKey())){
													oneInputMetrics.put(e.getKey(),e.getValue());
												}
									        }
											bestInput=moveNode;
										}
									}
								}
							}
						}
						if(!inputMatches){
							inputsMatch=false;
							/* vpapa: may be there exist other sets of input like in the
								case of parallel workflows e.g. Wind_Demo_o_Postgres. Break
								if all inputs have been checked. Until then continue
							*/
							if( i < inputs){
								logger.info( "Trying next inputs");
								continue;
							}
							else{
								break;
							}
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
					}//end of for (int i = 0; i < inputs; i++)
					if(inputsMatch){
						logger.info("all inputs match");
						int i =0;
						for(WorkflowNode bin : bestInputs){
							WorkflowNode tin = temp.inputs.get(i);
							logger.info("copy path from: "+bin.getName()+" to "+tin.getName());
							if(bin.isOperator){
								//move
								bin.operator.copyExecVariables(tin.dataset,0,bin.inputs);
							}
							else{
								bin.dataset.copyExecVariables(tin.dataset,0);
								bin.dataset.copyOptimization(tin.dataset);

							}
							i++;
						}


						/* vpapa: move out some common defitions in the following if else
							statement
						*/
						Double prevCost = 0.0;
						Double optCost	= 0.0;
						HashMap<String,Double> nextMetrics = null;
						HashMap<String,Double> bestInputMetrics = new HashMap<String, Double>();
						/* vpapa: the operator may not have any inputs if it is a generator for
							example. Thus minCostsForInput is
						*/
						if( !minCostsForInput.isEmpty()){
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
								bestInputMetrics.put(m, operatorInputCost);
							}
						}
						else{
							/* vpapa: whether inputs exist or not, this operator
								must be in the plan
							*/
							logger.info( "Processing kind of generator operator and"
										+ " thus input metrics are set manually");
							bestInputMetrics.put( "execTime", temp.getCost());
							bestInputs = new ArrayList< WorkflowNode>();
						}
						prevCost 	= computePolicyFunction(bestInputMetrics, materializedWorkflow.function);
						nextMetrics = op.getOptimalPolicyCost(bestInputMetrics, bestInputs, materializedWorkflow.function);
						
						
						optCost = computePolicyFunction(nextMetrics, materializedWorkflow.function);
						

						temp.setExecTime(nextMetrics.get("execTime")-bestInputMetrics.get("execTime"));
						temp.setOptimalCost(optCost-prevCost);

						for(Entry<String, Double> e : nextMetrics.entrySet()){
							if(bestInputMetrics.containsKey(e.getKey())){
								bestInputMetrics.put(e.getKey(),e.getValue());
							}
				        }
						
						plan.add(temp);

						//int outputs =Integer.parseInt(op.getParameter("Constraints.Output.number"));
						int outN=0;
						WorkflowNode tempOutputNode = null;
						Dataset tempOutput = null;
						//System.out.println(fromName);
						logger.info( "Outputs are: " + outputs);
						for (WorkflowNode out : outputs) {
							tempOutputNode = new WorkflowNode(false, false,"");
							tempOutput = new Dataset("t"+materializedWorkflow.count);
							materializedWorkflow.count++;
                            logger.info( "Call outputFor() for operator: " + op.opName);
                            logger.info( "with tempOutput: " + tempOutput);
                            logger.info( "outN: " + outN);
                            logger.info( "nextMetrics: " + nextMetrics);
                            logger.info( "temp.inputs: " + temp.inputs);
                            try{
								op.outputFor(tempOutput, outN, nextMetrics, temp.inputs);
                            }
                            catch( NullPointerException npe){
	                           	logger.info( "ERROR: For operator " + op.opName + " there is a");
	                           	logger.info( "mismatch between the Constraints.Output and");
	                           	logger.info( "Execution.Output properties inside its description");
	                           	logger.info( "file. Or maybe, these properties match between them");
	                           	logger.info( "but they may have a mismatch with the graph file");
	                           	logger.info( "of the workflow where this operator belongs, e.g. from");
	                           	logger.info( "the graph file the operatos has x outputs but in the");
	                           	logger.info( "description file y outputs where declared.");
                            }

							//tempOutput.outputFor(op, 0, temp.inputs);
							tempOutputNode.setDataset(tempOutput);
							tempOutputNode.addInput(temp);
							logger.info( "out.getName(): " + out.getName() + " fromName: " + fromName);
							if(out.getName().equals(fromName)){
								ret.add(tempOutputNode);
								plan.add(tempOutputNode);
								//System.out.println(nextMetrics);
								dpTable.addRecord(tempOutput, plan, optCost, bestInputMetrics);
							}
							else{
								out.inputs.add(tempOutputNode);
								ArrayList<WorkflowNode> tp = new ArrayList<>();
								tp.add(tempOutputNode);
								//System.out.println(nextMetrics);
								HashMap<String,Double> metrics = new HashMap<String, Double>();
								for(String m : materializedWorkflow.groupInputs.keySet()){
									metrics.put(m, 0.0);
								}
								dpTable.addRecord(tempOutput, tp, new Double(0), metrics);
								dpTable.addInputs(out.dataset, tp);
							}

							outN++;
						}
					}
				}
			}//end of if operator is abstract
			else{

			}
		}//end of if WorkflowNode is operator
		else{
			if(isAbstract){

				/*WorkflowNode temp = new WorkflowNode(false, false);
				temp.setDataset(dataset);
				for(List<WorkflowNode> l : materializedInputs){
					temp.addInputs(l);
				}
				ret.add(temp);*/
				for(List<WorkflowNode> l : materializedInputs){
					for(WorkflowNode tl : l){
						tl.setAbstractName(getName());
					}
					ret.addAll(l);
				}
				dpTable.addRecord(dataset, ret, new Double(0), new HashMap<String,Double>());
			}
			else{
				temp = new WorkflowNode(false, false, getName());
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
		}//end of else WorkflowNode is dataset
		logger.info( "Processed : " + toStringNorecursive());
		return ret;
	}//end of materialize

	public void setExecTime(Double execTime) {
		this.execTime=execTime;
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
				/* vpapa: an abstract operator may be included into an abstract
					workflow, but however may be missing from abstractOperators
					folder
				*/
				try{
					ret+=abstractOperator.opName;
				}
				catch( NullPointerException npe){
					System.out.println( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					logger.info( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					npe.printStackTrace();
				}
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
			/* vpapa: similar to toStringNorecursive() case */
				try{
					ret+=abstractOperator.opName;
				}
				catch( NullPointerException npe){
					System.out.println( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					logger.info( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					npe.printStackTrace();
				}
			else
				ret+=operator.opName;
		}
		else{
			/* vpapa: similar to toStringNorecursive() case */
			if( dataset != null)
				ret += dataset.datasetName;
			else
				ret += "noDataset";
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

	@Override
	public String toString() {
		String ret = "";
		if(isOperator){
			if(isAbstract)
				/* vpapa: similar to toStringNorecursive() case */
				try{
					ret+=abstractOperator.opName;
				}
				catch( NullPointerException npe){
					System.out.println( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					logger.info( "ERROR: The operator " + abstractOperator.opName + " does not exist!"
										+ "Check at least that there is a file named " + abstractOperator.opName
										+ " into abstractOperators folder");
					npe.printStackTrace();
				}
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
		//logger.info("Check :"+toStringNorecursive());
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
			//logger.info("running");
			return "running";
		}
		else{
			//logger.info("stopped");
			return "stopped";
		}
	}

	public void toWorkflowDictionary(WorkflowDictionary ret, HashMap<String, List<WorkflowNode>> bestPlans, String delimiter, List<WorkflowNode> targets) throws NumberFormatException, EvaluationException {
		if(!visited){
			OperatorDictionary op= new OperatorDictionary(getAbstractName(), toStringNorecursive(), String.format( "%.2f", getCost() ),String.format( "%.2f", getExecTime() ),
					getStatus(bestPlans), isOperator+"", isAbstract+"", toKeyValueString(delimiter), targets.contains(this));

			for(WorkflowNode n : inputs){
				op.addInput(n.toStringNorecursive());
				n.toWorkflowDictionary(ret, bestPlans, delimiter, targets);
			}
			for(WorkflowNode n : outputs){
				op.addOutput(n.toStringNorecursive());
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
				/* vpapa: may this WorkflowNode does not have any datasets due it
					does not have any inputs like in the case of a generator operator
				*/
				if( dataset != null)
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
		if(isOperator){
			int i=0;
			for(WorkflowNode n : inputs){
				graphWritter.write(n.toStringNorecursive() +","+toStringNorecursive()+","+i);
				graphWritter.newLine();
				i++;
			}
			i=0;
			for(WorkflowNode n : outputs){
				graphWritter.write(toStringNorecursive()+","+n.toStringNorecursive()+","+i);
				graphWritter.newLine();
				i++;
			}
		}
	}

	public void graphToStringRecursive(BufferedWriter graphWritter) throws IOException {

		if(!visited && isOperator){
			int i=0;
			for(WorkflowNode n : inputs){
				graphWritter.write(n.toStringNorecursive() +","+toStringNorecursive()+","+i);
				graphWritter.newLine();
				i++;
			}
			i=0;
			for(WorkflowNode n : outputs){
				graphWritter.write(toStringNorecursive()+","+n.toStringNorecursive()+","+i);
				graphWritter.newLine();
				i++;
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


	public String getInMetrics() {
		
		String ret ="";
		if(!isOperator)
			return "";
		else{
			for (String inVar : operator.inputSpace.keySet()) {
				String[] s = inVar.split("\\.");
				String inVar1= inVar.replace('.', '@');
				String val = null;
				if (s[0].startsWith("In")) {
					int index = Integer.parseInt(s[0].substring((s[0].length() - 1)));
					logger.info( "Operator inputs are: " + inputs);
					WorkflowNode n = inputs.get(index);
					if (!n.isOperator) {
						val = n.dataset.getParameter("Optimization." + s[1]);
						if(val==null){
							val = operator.getParameter("SelectedParam." + s[1]);
							if(val==null){
								val="0";
							}
						}
					}
				} else {
					val = operator.getParameter("SelectedParam." + s[0]);
					if(val==null){
						val="0";
					}
				}
				
				Double v = Double.parseDouble(val);
				ret+=inVar1+"="+v+" ";
			}
		}
		return ret;
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
			    		logger.info( "newArg: " + newArg);
			    		if( newArg == null){
			    			logger.info( "ERROR: For input dataset " + n.dataset.datasetName + " the requested parameter");
			    			logger.info( "Execution." + parameter + " does not exist! This parameter has been asked");
			    			logger.info( "from operator " + operator.opName + " as a property of input In" + index + ".");
			    			logger.info( "To solve this, make sure that the input of this dataset, " + n.dataset.datasetName + ",");
			    			logger.info( "i.e. the operator that corresponds to input In" + index + " defines a property");
			    			logger.info( "'Execution.Output" + index + "." + parameter + ".");
			    		}
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
		    	else if(arg.startsWith("Optimization")){
		    		String newArg = operator.getParameter(arg);
		    		logger.info( "newArg: " + newArg);
		    		if( newArg == null){
		    			newArg = operator.getParameter("SelectedParams."+arg.substring(arg.indexOf(".")+1));
		    		}
		    		logger.info( "newArg: " + newArg);
		    		arg=newArg;
		    		
		    	}
		    	/* vpapa: an execution argument may be surrounded by double or single quotes
		    		in which case no double or single quotes should be added and the argument
		    		should be taken as is. If the argument is not surrounded by double quotes
		    		and it also contains spaces, then it must be contained by double or single
		    		quotes. This way bash interpreter can understand correctly the execution
		    		arguments and the operator can be executed if no other error rises.
		    	*/
		    	//does argument starts and ends with double or single quotes?
		    	if( ( arg.startsWith( "\"") && arg.endsWith( "\"")) || ( arg.startsWith( "'") && arg.endsWith( "'"))){
		    		//return as is
		    		ret += arg + " ";
		    	}
		    	else{
					//argument is not surrounded by double( single) quotes, does it contain spaces?
					if(arg.contains(" ")){
						//surround argument by double quotes
						ret += "\"" + arg + "\"" + " ";
					}
					else{
						//argument is not surrounded by double( single) quotes and it does
						//not contain spaces
						ret += arg + " ";
					}  	
		    	}
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

	public Double getExecTime() {
		if(isOperator && !isAbstract){
    		return execTime;
		}
		else{
    		return 0.0;
		}
	}

}
