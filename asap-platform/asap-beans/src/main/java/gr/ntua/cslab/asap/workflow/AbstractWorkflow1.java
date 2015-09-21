package gr.ntua.cslab.asap.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import gr.ntua.cslab.asap.staticLibraries.AbstractOperatorLibrary;
import gr.ntua.cslab.asap.staticLibraries.DatasetLibrary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.MaterializedOperators;
import gr.ntua.cslab.asap.operators.NodeName;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

public class AbstractWorkflow1 {
	private List<WorkflowNode> targets;
	private List<WorkflowNode> abstractInputs;
	private HashMap<String,WorkflowNode> workflowNodes;
	public String name, directory;
	private static Logger logger = Logger.getLogger(AbstractWorkflow1.class.getName());
	
	public HashMap<String,String> groupInputs;
	public String optimizationFunction;
	public String functionTarget;

	@Override
	public String toString() {
		return targets.toString();
	}

	public AbstractWorkflow1(String name) {
		this.directory = "/tmp";
		this.name=name;
		targets = new ArrayList<WorkflowNode>();
		workflowNodes = new HashMap<String, WorkflowNode>();
	}
	
	public AbstractWorkflow1(String name, String directory) {
		this.directory = directory;
		this.name=name;
		targets = new ArrayList<WorkflowNode>();
		workflowNodes = new HashMap<String, WorkflowNode>();
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

	public MaterializedWorkflow1 materialize(String metric, String nameExtention, String policy) throws Exception {
		OperatorLibrary.moveid=0;
		parsePolicy(policy);
		String fullName=name+"_"+nameExtention;
		MaterializedWorkflow1 materializedWorkflow = new MaterializedWorkflow1(fullName, MaterializedWorkflowLibrary.getWorkflowDirectory()+"/"+fullName);
		materializedWorkflow.setPolicy(groupInputs, optimizationFunction, functionTarget);
		Workflow1DPTable dpTable = new Workflow1DPTable();
		for(WorkflowNode t : targets){
			List<WorkflowNode> l = t.materialize(metric, materializedWorkflow,dpTable);
			WorkflowNode temp = new WorkflowNode(false, false);
			temp.setDataset(t.dataset);
			//System.out.println(l+"fsdgd");
			temp.addInputs(l);
			materializedWorkflow.addTarget(temp);
			Double bestCost=0.0;
			List<WorkflowNode> bestPlan=null;
			if(functionTarget.contains("min")){
				bestCost=Double.MAX_VALUE;
				for(WorkflowNode r : l){
					Double tempCost = dpTable.getCost(r.dataset);
					if(tempCost<bestCost){
						bestCost=tempCost;
						bestPlan=dpTable.getPlan(r.dataset);
					}
				}
			}
			else if(functionTarget.contains("max")){
				bestCost = -Double.MAX_VALUE;
				for(WorkflowNode r : l){
					Double tempCost = dpTable.getCost(r.dataset);
					if(tempCost>bestCost){
						bestCost=tempCost;
						bestPlan=dpTable.getPlan(r.dataset);
					}
				}
			}
			bestPlan.add(t);
			materializedWorkflow.setBestPlan(t.toStringNorecursive(), bestPlan);
			logger.info("Optimal cost: "+bestCost);
			materializedWorkflow.optimalCost=bestCost;
		}
		
		return materializedWorkflow;
	}
	
	public String parsePolicy(String policy) {
		groupInputs = new HashMap<String, String>();
		String[] p = policy.split("\n");
		for (int i = 0; i < p.length; i++) {
			String[] p1 = p[i].split(",");
			if(p1[0].equals("groupInputs")){
				groupInputs.put(p1[1], p1[2]);
			}
			else if(p1[0].equals("function")){
				optimizationFunction=p1[1];
				functionTarget=p1[2];
			}
		}
		return optimizationFunction;
		//System.out.println(functionTarget);
	}

	public static String getPolicyFromString(String policy) {
		String[] p = policy.split("\n");
		for (int i = 0; i < p.length; i++) {
			String[] p1 = p[i].split(",");
			if(p1[0].equals("function")){
				return p1[1];
			}
		}
		return "";
		//System.out.println(functionTarget);
	}
	
	public void writeToDir(String directory) throws Exception {

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
	

	public void readFromDir(String directory) throws IOException {
		HashMap<String,WorkflowNode> nodes = new HashMap<String, WorkflowNode>();
		File folder = new File(directory+"/operators");
		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && !files[i].isHidden()) {
				WorkflowNode n = new WorkflowNode(true, true);
				AbstractOperator temp = new AbstractOperator(files[i].getName());
				temp.readPropertiesFromFile(files[i]);
				n.setAbstractOperator(temp);
				nodes.put(temp.opName, n);
			} 
		}
		folder = new File(directory+"/datasets");
		files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && !files[i].isHidden()) {
				WorkflowNode n =null;
				if(files[i].getName().startsWith("d")){
					n = new WorkflowNode(false, true);
				}
				else{
					n = new WorkflowNode(false, false);
				}
				Dataset temp = new Dataset(files[i].getName());
				temp.readPropertiesFromFile(files[i]);
				n.setDataset(temp);
				nodes.put(temp.datasetName, n);
			} 
		}
		workflowNodes.putAll(nodes);
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
				WorkflowNode src = nodes.get(e[0]);
				WorkflowNode dest = nodes.get(e[1]);
				dest.inputs.add(src);
			}
		}
		br.close();
	}
	

	public void refresh() {
		for(Entry<String, WorkflowNode> e : workflowNodes.entrySet()){
			if(e.getValue().isOperator){
				AbstractOperator abstractOp = AbstractOperatorLibrary.getOperator(e.getKey());
				workflowNodes.get(e.getKey()).abstractOperator=abstractOp;
			}
			else if(!e.getValue().isAbstract){
				Dataset d = DatasetLibrary.getDataset(e.getKey());
				workflowNodes.get(e.getKey()).dataset=d;
			}
		}
		
	}

	
	public String graphToString() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		BufferedWriter graphWritter = new BufferedWriter(new OutputStreamWriter(bs));

		for(WorkflowNode n : workflowNodes.values()){
			n.graphToString(graphWritter);
		}

		for(WorkflowNode t : targets){
			graphWritter.write(t.toStringNorecursive() +",$$target");
			graphWritter.newLine();
		}
		
		graphWritter.close();
		return bs.toString("UTF-8");
	}
	
	public String graphToStringRecursive() throws IOException {

		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		BufferedWriter graphWritter = new BufferedWriter(new OutputStreamWriter(bs));
    	

		for(WorkflowNode t : targets){
			t.graphToStringRecursive(graphWritter);
			graphWritter.write(t.toStringNorecursive() +",$$target");
			graphWritter.newLine();
		}
		
		graphWritter.close();
		return bs.toString("UTF-8");
	}


	public void changeEdges(String workflowGraph) throws IOException {
		targets= new ArrayList<WorkflowNode>();
		for(WorkflowNode n : workflowNodes.values()){
			n.inputs = new ArrayList<WorkflowNode>();
		}
		ByteArrayInputStream fis = new ByteArrayInputStream(workflowGraph.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] e =line.split(",");
			if(e[1].equals("$$target")){
				this.targets.add(workflowNodes.get(e[0]));
			}
			else{
				WorkflowNode src = workflowNodes.get(e[0]);
				WorkflowNode dest = workflowNodes.get(e[1]);
				dest.inputs.add(src);
			}
		}
		br.close();
	}

	public void addNode(String type, String name) {
		int t = Integer.parseInt(type);
		WorkflowNode n =null;
		switch (t) {
		case 1:
			n = new WorkflowNode(true, true);
			n.setAbstractOperator(AbstractOperatorLibrary.getOperator(name));
			break;
		case 2:
			n = new WorkflowNode(true, false);
			n.setOperator(OperatorLibrary.getOperator(name));
			break;
		case 3:
			n = new WorkflowNode(false, true);
			n.setDataset(new Dataset(name));
			break;
		case 4:
			n = new WorkflowNode(false, false);
			n.setDataset(DatasetLibrary.getDataset(name));
			
			break;

		default:
			n = new WorkflowNode(false, false);
			break;
		}
		
		workflowNodes.put(name,n);
		
	}

	public void removeNode(String type, String name) {
		WorkflowNode n = workflowNodes.remove(name);
		for(WorkflowNode n1 : workflowNodes.values()){
			List<WorkflowNode> l =  new ArrayList<WorkflowNode>( n1.inputs);
			int index = 0;
			for(WorkflowNode n2 :l){
				if(n2.compareTo(n)==0){
					n1.inputs.remove(index);
				}
				index++;
			}
		}
	}

	public WorkflowDictionary toWorkflowDictionary(String delimiter) throws NumberFormatException, EvaluationException {
		WorkflowDictionary ret = new WorkflowDictionary();
		for(WorkflowNode n : workflowNodes.values()){
	    	OperatorDictionary op = new OperatorDictionary(n.toStringNorecursive(), n.getCost()+"", 
	    			n.getStatus(new HashMap<String, List<WorkflowNode>>()), n.isOperator+"", n.toKeyValueString(delimiter), targets.contains(n));

			for(WorkflowNode in : n.inputs){
				op.addInput(in.toStringNorecursive());
			}
	    	ret.addOperator(op);
		}
		return ret;
	}
	
	public WorkflowDictionary toWorkflowDictionaryRecursive(String delimiter) throws NumberFormatException, EvaluationException {
		for(WorkflowNode t : targets){
			t.setAllNotVisited();
		}
		WorkflowDictionary ret = new WorkflowDictionary();
    	for(WorkflowNode target : targets){
    		target.toWorkflowDictionary(ret, new HashMap<String, List<WorkflowNode>>(), delimiter, targets);
    	}
		return ret;
	}
	
	
	public static void main(String[] args) throws Exception{


		Evaluator evaluator = new Evaluator();

		try {

			/**
			 * This sample shows a basic math function.
			 */
			System.out.println(evaluator.evaluate("abs(-1)"));

			/**
			 * This sample shows a math function used in a more complex
			 * mathematical expression.
			 */
			System.out.println(evaluator.evaluate("pow(3,3) "));

			/**
			 * This sample shows another math function.
			 */
			System.out.println(evaluator.evaluate("acos(0.1)"));

			/**
			 * This sample shows an invalid expression. The "round" function
			 * requires an argument.
			 */
			System.out.println("An exception is expected in the "
					+ "next evaluation.");
			System.out.println(evaluator.evaluate("round(3.7)"));
		} catch (EvaluationException ee) {
			System.out.println(ee);
		}
		
		System.exit(0);

		MaterializedOperators library =  new MaterializedOperators();
		AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("test");
		Dataset d1 = new Dataset("hbaseDataset");
		d1.add("Constraints.DataInfo.Attributes.number","2");
		d1.add("Constraints.DataInfo.Attributes.Atr1.type","ByteWritable");
		d1.add("Constraints.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.key","Atr1");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.value","Atr2");
		d1.add("Constraints.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		d1.add("Optimization.size","1TB");
		d1.add("Optimization.uniqueKeys","1.3 billion");

		WorkflowNode t1 = new WorkflowNode(false,false);
		t1.setDataset(d1);
		//d1.writeToPropertiesFile(d1.datasetName);
		
		Dataset d2 = new Dataset("mySQLDataset");
		d2.add("Constraints.DataInfo.Attributes.number","2");
		d2.add("Constraints.DataInfo.Attributes.Atr1.type","Varchar");
		d2.add("Constraints.DataInfo.Attributes.Atr2.type","Varchar");
		d2.add("Constraints.Engine.DB.Relational.MySQL.schema","...");
		d2.add("Constraints.Engine.DB.Relational.MySQL.location","127.0.0.1");
		d2.add("Optimization.size","1GB");
		d2.add("Optimization.uniqueKeys","1 million");

		WorkflowNode t2 = new WorkflowNode(false,false);
		t2.setDataset(d2);
		//d2.writeToPropertiesFile(d2.datasetName);

		AbstractOperator abstractOp = new AbstractOperator("JoinOp");
		abstractOp.add("Constraints.Input.number","2");
		abstractOp.add("Constraints.Output.number","1");
		abstractOp.add("Constraints.Input0.DataInfo.Attributes.number","2");
		abstractOp.add("Constraints.Input1.DataInfo.Attributes.number","2");
		abstractOp.add("Constraints.Output0.DataInfo.Attributes.number","2");
		abstractOp.addRegex(new NodeName("Constraints.OpSpecification.Algorithm.Join", new NodeName(".*", null, true), false), ".*");

		WorkflowNode op1 = new WorkflowNode(true,true);
		op1.setAbstractOperator(abstractOp);
		//abstractOp.writeToPropertiesFile(abstractOp.opName);

		AbstractOperator abstractOp1 = new AbstractOperator("SortOp");
		abstractOp1.add("Constraints.Input.number","1");
		abstractOp1.add("Constraints.Output.number","1");
		abstractOp1.add("Constraints.Input0.DataInfo.Attributes.number","2");
		abstractOp1.add("Constraints.Output0.DataInfo.Attributes.number","2");
		abstractOp1.addRegex(new NodeName("Constraints.OpSpecification.Algorithm.Sort", new NodeName(".*", null, true), false), ".*");

		WorkflowNode op2 = new WorkflowNode(true,true);
		op2.setAbstractOperator(abstractOp1);
		//abstractOp1.writeToPropertiesFile(abstractOp1.opName);
		
		Dataset d3 = new Dataset("d3");
		WorkflowNode t3 = new WorkflowNode(false,true);
		t3.setDataset(d3);
		Dataset d4 = new Dataset("d4");
		WorkflowNode t4 = new WorkflowNode(false,true);
		t4.setDataset(d4);
		
		op1.addInput(t1);
		op1.addInput(t2);
		t3.addInput(op1);
		op2.addInput(t3);
		t4.addInput(op2);
		abstractWorkflow.addTarget(t4);

		abstractWorkflow.writeToDir("asapLibrary/abstractWorkflows/DataAnalytics");
		System.exit(0);
		MaterializedWorkflow1 mw = abstractWorkflow.materialize("execTime", "t","");
		System.out.println(abstractWorkflow);
		System.out.println(mw);
		mw.printNodes();
		
	}







}
