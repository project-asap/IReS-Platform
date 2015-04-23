package gr.ntua.cslab.asap.workflow;

import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;

public class MaterializedWorkflow1 {
	private List<WorkflowNode> targets;
	private HashMap<String,List<WorkflowNode>> bestPlans;
	public double optimalCost;
	public int count;
	public String name;
	public HashMap<String, String> groupInputs;
	public String function;
	public String functionTarget;
	private static Logger logger = Logger.getLogger(MaterializedWorkflow1.class.getName());
	public HashMap<String,WorkflowNode> nodes;
	public String directory;
	
	@Override
	public String toString() {
		return targets.toString();
	}
	
	public MaterializedWorkflow1(String name, String directory) {
		this.directory = directory;
		this.name = name;
		targets = new ArrayList<WorkflowNode>();
		bestPlans = new HashMap<String, List<WorkflowNode>>();
		optimalCost=0.0;
		count=0;
		nodes = new HashMap<String, WorkflowNode>();
	}

	public void setBestPlan(String target, List<WorkflowNode> plan){
		logger.info("Target: "+target);
		logger.info("Best plan: ");
		for(WorkflowNode w : plan)
			logger.info(w.toStringNorecursive());
		bestPlans.put(target, plan);
	}
	
	public void setAllNotVisited(){

		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}
	}
	
	public void printNodes() {
		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}
		for(WorkflowNode t : targets){
			t.printNodes();
		}
	}
	
	public WorkflowDictionary toWorkflowDictionary(String delimiter) throws NumberFormatException, EvaluationException {
		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}
		WorkflowDictionary ret = new WorkflowDictionary();
    	for(WorkflowNode target : targets){
    		target.toWorkflowDictionary(ret, bestPlans, delimiter, targets);
    	}
		return ret;
	}
	
	public void readFromWorkflowDictionary(WorkflowDictionary workflow) throws Exception {
		nodes = new HashMap<String, WorkflowNode>();
		HashSet<String> nodesActive = new HashSet<String>();
		for(OperatorDictionary op : workflow.getOperators()){
			if(op.getStatus().equals("warn")){
				nodesActive.add(op.getName());
				if(op.getIsOperator().equals("true")){
					WorkflowNode n = new WorkflowNode(true, false);
					Operator temp = new Operator(op.getName(),"");
					temp.readPropertiesFromString(op.getDescription());
					n.setOperator(temp);
					nodes.put(temp.opName, n);
				}
				else{
					WorkflowNode n = new WorkflowNode(false, false);
					Dataset temp = new Dataset(op.getName());
					temp.readPropertiesFromString(op.getDescription());
					n.setDataset(temp);
					nodes.put(temp.datasetName, n);
					if(op.isTarget())
						this.targets.add(n);
				}
			}
		}
		for(OperatorDictionary op : workflow.getOperators()){
			if(nodesActive.contains(op.getName())){
				WorkflowNode dest = nodes.get(op.getName());
				for(String s : op.getInput()){
					if(nodesActive.contains(s)){
						WorkflowNode src = nodes.get(s);
						dest.inputs.add(src);
					}
				}
			}
		}
	}
	

	public void writeToDir() throws Exception {
		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}
		
        File workflowDir = new File(directory);
        if (!workflowDir.exists()) {
        	workflowDir.mkdirs();
        }
        File opDir = new File(directory+"/operators");
        if (!opDir.exists()) {
        	opDir.mkdirs();
        }
        File datasetDir = new File(directory+"/datasets");
        if (!datasetDir.exists()) {
        	datasetDir.mkdirs();
        }
        File edgeGraph = new File(directory+"/graph");
    	FileOutputStream fos = new FileOutputStream(edgeGraph);
    	 
    	BufferedWriter graphWritter = new BufferedWriter(new OutputStreamWriter(fos));
    	

		for(WorkflowNode t : targets){
			t.writeToDir(directory+"/operators",directory+"/datasets",graphWritter);
			graphWritter.write(t.toStringNorecursive() +",$$target");
			graphWritter.newLine();
		}
    	
		graphWritter.close();
        
        
	}

	public void readFromDir() throws Exception {
		nodes = new HashMap<String, WorkflowNode>();
		File folder = new File(directory+"/operators");
		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				WorkflowNode n = new WorkflowNode(true, false);
				Operator temp = new Operator(files[i].getName(),files[i].toString());
				temp.readFromDir();
				n.setOperator(temp);
				nodes.put(temp.opName, n);
			} 
		}
		folder = new File(directory+"/datasets");
		files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				WorkflowNode n = new WorkflowNode(false, false);
				Dataset temp = new Dataset(files[i].getName());
				temp.readPropertiesFromFile(files[i]);
				n.setDataset(temp);
				nodes.put(temp.datasetName, n);
			} 
		}
		
		File edgeGraph = new File(directory+"/graph");
		FileInputStream fis = new FileInputStream(edgeGraph);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] e =line.split(",");
			if(e[1].equals("$$target")){
				this.targets.add(nodes.get(e[0]));
			}
			else{
				//System.out.println(e[0]+" "+e[1]);
				WorkflowNode src = nodes.get(e[0]);
				WorkflowNode dest = nodes.get(e[1]);
				dest.inputs.add(src);
			}
		}
		br.close();
	}
	
	public void addTarget(WorkflowNode target) {
		targets.add(target);
	}

	public void addTargets(List<WorkflowNode> targets) {
		this.targets.addAll(targets);
	}
	
	public List<WorkflowNode> getTargets() {
		return targets;
	}
	
	

	public static void main(String[] args) throws Exception {
		/*MaterializedWorkflow1 mw = new MaterializedWorkflow1();
		
		WorkflowNode t = new WorkflowNode(false,false);
		t.setDataset(new Dataset("out2"));
		mw.addTarget(t);

		WorkflowNode t1 = new WorkflowNode(true,false);
		t1.setOperator(new Operator("op2_1"));
		t.addInput(t1);
		WorkflowNode t2 = new WorkflowNode(true,false);
		t2.setOperator(new Operator("op2_2"));
		t.addInput(t2);
		

		WorkflowNode t31 = new WorkflowNode(false,false);
		t31.setDataset(new Dataset("in1.1"));
		WorkflowNode t32 = new WorkflowNode(false,false);
		t32.setDataset(new Dataset("in1.2"));
		t1.addInput(t31);
		t1.addInput(t32);


		WorkflowNode t33 = new WorkflowNode(false,false);
		t33.setDataset(new Dataset("in2.1"));
		WorkflowNode t34 = new WorkflowNode(false,false);
		t34.setDataset(new Dataset("in2.2"));
		t2.addInput(t33);
		t2.addInput(t34);
		
		System.out.println(mw);
		mw.printNodes();
		*/
		MaterializedWorkflow1 mw = new MaterializedWorkflow1("sd", "asapLibrary/workflows/latest");
		
		mw.readFromDir();
		System.out.println(mw);
	}

	public void setPolicy(HashMap<String, String> groupInputs, String function, String functionTarget) {
		this.groupInputs = groupInputs;
		this.function = function;
		this.functionTarget = functionTarget;
	}




	
	
}
