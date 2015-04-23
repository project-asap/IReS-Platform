package gr.ntua.cslab.asap.workflow;

import gr.ntua.cslab.asap.operators.Dataset;

import java.util.HashMap;

public class WorkflowDPTable {
	private HashMap<Dataset,HashMap<Dataset,Workflow>> dpTable;

	public WorkflowDPTable() {
		dpTable = new HashMap<Dataset, HashMap<Dataset,Workflow>>();
	}
	
	public void addRecord(Dataset abstractDataset, Dataset materializedDataset, Workflow workflow){
		HashMap<Dataset, Workflow> map = dpTable.get(abstractDataset);
		if(map==null){
			map = new HashMap<Dataset, Workflow>();
			dpTable.put(abstractDataset, map);
		}
		map.put(materializedDataset, workflow);
	}
	
	public HashMap<Dataset, Workflow> getCost(Dataset abstractDataset){
		HashMap<Dataset, Workflow> map = dpTable.get(abstractDataset);
		if(map==null){
			return null;
		}
		return map;
	}
	
}
