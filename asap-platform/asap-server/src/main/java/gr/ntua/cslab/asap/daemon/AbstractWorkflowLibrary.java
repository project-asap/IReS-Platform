package gr.ntua.cslab.asap.daemon;

import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	
	public static WorkflowDictionary getWorkflow(String name, String delimiter) throws NumberFormatException, EvaluationException{
		return abstractWorkflows.get(name).toWorkflowDictionary(delimiter);
	}

	public static List<String> getWorkflows() {
		return new ArrayList<String>(abstractWorkflows.keySet());
	}

	public static String getMaterializedWorkflow(String workflowName, String policy) throws Exception {
		AbstractWorkflow1 aw = abstractWorkflows.get(workflowName);
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
		Date date = new Date();
		MaterializedWorkflow1 mw = aw.materialize("execTime", dateFormat.format(date), policy);
		MaterializedWorkflowLibrary.add(mw);
		return mw.name;
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

}
