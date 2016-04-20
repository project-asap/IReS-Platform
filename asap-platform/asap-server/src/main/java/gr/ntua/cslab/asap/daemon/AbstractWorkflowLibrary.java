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


package gr.ntua.cslab.asap.daemon;

import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.daemon.rest.TransformWorkflows;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;

public class AbstractWorkflowLibrary {
	private static HashMap<String,AbstractWorkflow1> abstractWorkflows;
	private static String workflowDirectory;

	public static void initialize(String directory) throws IOException{
		workflowDirectory = directory;
		abstractWorkflows = new HashMap<String, AbstractWorkflow1>();
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
		        Logger.getLogger(OperatorLibrary.class.getName()).info("Loading abstract workflow: " + listOfFiles[i].getName());
		        AbstractWorkflow1 w = new AbstractWorkflow1(listOfFiles[i].getName(), listOfFiles[i].getPath());
		        w.readFromDir(listOfFiles[i].getPath());
		        abstractWorkflows.put(listOfFiles[i].getName(), w);
		    }
		}
	}
	
	public static WorkflowDictionary getWorkflow(String name, String delimiter) throws Exception{
		return abstractWorkflows.get(name).toWorkflowDictionary(delimiter);
	}

	public static List<String> getWorkflows() {
		return new ArrayList<String>(abstractWorkflows.keySet());
	}

	public static String getMaterializedWorkflow(String workflowName, String policy, String parameters) throws Exception {
		Properties params =parseParameters(parameters);
		
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		for(Entry<Object, Object> e: params.entrySet()){
			String k = (String) e.getKey();
			String v = (String) e.getValue();
			String[] s = k.split("\\.");
			k=k.substring(k.indexOf('.')+1, k.length());
			Operator op = OperatorLibrary.getOperator(s[0]);
			if(op!=null)
				op.add(k, v);
			WorkflowNode node = aw.workflowNodes.get(s[0]);
			if(node!=null){
				if(!node.isOperator)
					node.dataset.add(k, v);
			}
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
		Date date = new Date();
		Long start = System.currentTimeMillis();
		
		MaterializedWorkflow1 mw = aw.materialize(dateFormat.format(date), policy);		
		Long stop = System.currentTimeMillis();
		

		for(Entry<Object, Object> e: params.entrySet()){
			String k = (String) e.getKey();
			String v = (String) e.getValue();
			String[] s = k.split("\\.");
			k=k.substring(k.indexOf('.')+1, k.length());
			//System.out.println(s[0]+" sdfsd "+mw.bestPlans.size());
			for(Entry<String, List<WorkflowNode>> e1:mw.bestPlans.entrySet()){
				for(WorkflowNode node : e1.getValue()){
					//System.out.println(node.getName());
					if(node.getName().contains(s[0])){
						//System.out.println(s[0]);
						//System.out.println(k+" "+v);
						if(node.isOperator){
							//System.out.println(k+" "+v);
							node.operator.add(k, v);
						}
						else{
							node.dataset.add(k, v);
						}
					}
				}
			}
		}
		MaterializedWorkflowLibrary.add(mw);
		return mw.name;
	}

	private static Properties parseParameters(String parameters) throws IOException {
		InputStream stream = new ByteArrayInputStream(parameters.getBytes());
		Properties props = new Properties();
		props.load(stream);
		stream.close();
		return props;
	}

	public static String getGraphDescription(String workflowName) throws IOException {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		return aw.graphToString();
	}


	public static void refresh(String workflowName) {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		aw.refresh();
	}
	
	public static void changeWorkflow(String workflowName, String workflowGraph) throws IOException {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		aw.changeEdges(workflowGraph);
		
	}
	
	public static void newWorkflow(String workflowName) {
		AbstractWorkflow1 aw = new AbstractWorkflow1(workflowName, workflowDirectory+"/"+workflowName);
		abstractWorkflows.put(workflowName, aw);
	}

	public static void addNode(String workflowName, String type, String name) {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		aw.addNode(type, name);
	}

	public static void removeNode(String workflowName, String type, String name) {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		aw.removeNode(type, name);
	}

	public static void addWorkflow(String workflowName, WorkflowDictionary workflow) throws IOException {
		
		AbstractWorkflow1 w = TransformWorkflows.tranformAbstractWorkflow(workflowName, workflowDirectory, workflow);
		
		abstractWorkflows.put(workflowName, w);
	}

	public static void removeWorkflow(String id) {
		abstractWorkflows.remove(id);
	}

}
