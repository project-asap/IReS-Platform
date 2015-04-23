package gr.ntua.cslab.asap.workflow;

import gr.ntua.cslab.asap.operators.Dataset;

import java.util.HashMap;
import java.util.List;

public class Workflow1DPTable {
	private HashMap<Dataset,List<WorkflowNode>> dpTable;
	private HashMap<Dataset,Double> dpCost;
	private HashMap<Dataset,HashMap<String,Double>> dpMetrics;

	public Workflow1DPTable() {
		dpTable = new HashMap<Dataset,List<WorkflowNode>>();
		dpCost = new HashMap<Dataset,Double>();
		dpMetrics = new HashMap<Dataset, HashMap<String,Double>>();
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
}
