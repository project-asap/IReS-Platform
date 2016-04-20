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
