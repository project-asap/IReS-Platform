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

import gr.ntua.cslab.asap.operators.Dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Workflow1DPTable {
	private HashMap<Dataset,List<WorkflowNode>> dpTable;
	private HashMap<Dataset,List<WorkflowNode>> movedDatasets;
	private HashMap<Dataset,Double> dpCost;
	private HashMap<Dataset,HashMap<String,Double>> dpMetrics;

	public Workflow1DPTable() {
		dpTable = new HashMap<Dataset,List<WorkflowNode>>();
		dpCost = new HashMap<Dataset,Double>();
		dpMetrics = new HashMap<Dataset, HashMap<String,Double>>();
		movedDatasets = new HashMap<Dataset,List<WorkflowNode>>();
	}

//	public void addInputs(Dataset dataset, ArrayList<WorkflowNode> plan) {
//		List<WorkflowNode> temp = dpTable.get(dataset);
//		if(temp!=null){
//			temp.addAll(plan);
//		}
//		else{
//			dpTable.put(dataset, plan);
//		}
//		
//	}
	
	public void addInputs(Dataset dataset, List<WorkflowNode> plan, Double cost, HashMap<String,Double> metrics) {
		List<WorkflowNode> temp = dpTable.get(dataset);
		if(temp!=null){
			temp.addAll(plan);
		}
		else{
			dpTable.put(dataset, plan);
		}
		dpCost.put(dataset,cost);
		dpMetrics.put(dataset,metrics);
		
	}

	public void addRecord(Dataset dataset, List<WorkflowNode> plan, Double cost, HashMap<String,Double> metrics){
		dpTable.put(dataset, plan);
		dpCost.put(dataset,cost);
		dpMetrics.put(dataset,metrics);
	}
	
	
	public Double getCost(Dataset dataset){
		Double value = dpCost.get(dataset);
		if(value==null)
			return Double.MAX_VALUE;
		else
			return value;
	}
	
	public List<WorkflowNode> getPlan(Dataset dataset){
		return dpTable.get(dataset);
	}
	
	public HashMap<String,Double> getMetrics(Dataset dataset){
		return dpMetrics.get(dataset);
	}

	public void addMovedDataset(Dataset dataset, WorkflowNode movedDataset) {
		List<WorkflowNode> temp = movedDatasets.get(dataset);
		if(temp!=null){
			temp.add(movedDataset);
		}
		else{
			List<WorkflowNode> v =  new ArrayList<>();
			v.add(movedDataset);
			movedDatasets.put(dataset, v);
		}
	}

	public List<WorkflowNode> getMovedDatasets(Dataset dataset){
		return movedDatasets.get(dataset);
	}
}
