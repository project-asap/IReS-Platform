package gr.ntua.cslab.asap.testMaterialization;

import gr.ntua.cslab.asap.daemon.AbstractWorkflowLibrary;
import gr.ntua.cslab.asap.daemon.rest.AbstractWorkflows;
import gr.ntua.cslab.asap.daemon.rest.Datasets;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.staticLibraries.AbstractOperatorLibrary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class TestMaterialization {
	static int count = 0;
	static HashMap<Integer,WorkflowNode> operators = new HashMap<>();
	public static double execute(String path, String testCase, int matches) throws Exception {
		
		String policy ="metrics,cost,execTime\n"+
				"groupInputs,execTime,max\n"+
				"groupInputs,cost,sum\n"+
				"function,execTime,min";
		AbstractWorkflow1 workflow = new AbstractWorkflow1("test");
		XMLReaderFactory xmlRFactory = new XMLReaderFactory(path);
		Hashtable<Integer, Object> nodes = xmlRFactory.getReader("job").readData(); 
		nodes = xmlRFactory.getReader("graphnode").readData(nodes); 
		
		
		for( Entry<Integer, Object> e:nodes.entrySet()){
			GraphNode n = (GraphNode) e.getValue();
			
			WorkflowNode tempOp = createOp(n.getParents().keySet().size(),n.getChildren().keySet().size());
			workflow.workflowNodes.put(tempOp.getName(), tempOp);
			operators.put(e.getKey(), tempOp);
			createMaterializedOps(n.getParents().keySet().size(),n.getChildren().keySet().size(),matches,tempOp.abstractOperator);
//			System.out.println(e.getKey()+" inputs: "+n.getParents().keySet().size()+" outputs: "+n.getChildren().keySet().size());
		}
		
		for( Entry<Integer, Object> e:nodes.entrySet()){
			GraphNode n = (GraphNode) e.getValue();
			WorkflowNode tempOp = operators.get(e.getKey());
			//add inputs
			if(n.getParents().size()==0){
				Dataset d = new Dataset("hdfs_file_"+count);
				d.readPropertiesFromFile(new File("/local/npapa/asap-server/asapLibrary/datasets/hdfs_file"));
				WorkflowNode t = new WorkflowNode(false,false,"hdfs_file_"+count);
				count++;
				t.setDataset(d);
				tempOp.addInput(0,t);
				t.addOutput(0,tempOp);
			}
			else{
				int i=0;
				for(Integer p : n.getParents().keySet()){

					WorkflowNode parentOp = operators.get(p);
					
					WorkflowNode d = createDataset();
					d.addInput(0, parentOp);
					d.addOutput(0,tempOp);
					
					tempOp.addInput(i,d);
					parentOp.addOutput(parentOp.outputs.size(),d);
					i++;
				}
			}
			
			//add outputs
			if(n.getChildren().size()==0){
				WorkflowNode d = createDataset();
				tempOp.addOutput(0,d);
				d.addInput(0,tempOp);
				workflow.addTarget(d);
			}
			
			
		}
		//System.out.println(workflow.graphToString());
		long start = System.currentTimeMillis();
		workflow.materialize("_test", policy);
		//System.out.println(testCase+" : "+(System.currentTimeMillis()-start)/1000.0);
		return (System.currentTimeMillis()-start)/1000.0;
		//AbstractWorkflowLibrary.addWorkflow(workflow.name, workflow.toWorkflowDictionary("\n"));
		//MaterializedWorkflowLibrary.add(workflow.materialize("_test", policy));
	} 
	
	public static WorkflowNode createDataset() throws IOException{
		Dataset d = new Dataset("d"+count);
		WorkflowNode t = new WorkflowNode(false,true,"d"+count);
		t.setDataset(d);
		count++;
		return t;
	}
	
	public static WorkflowNode createOp(int inputs, int outputs) throws IOException{
		if(inputs==0)
			inputs=1;
		if(outputs==0)
			outputs=1;
		
		AbstractOperator aop = new AbstractOperator("testop"+count);
		aop.add("Constraints.Input.number", inputs+"");
		aop.add("Constraints.Output.number", outputs+"");
		aop.add("Constraints.OpSpecification.Algorithm.name", "testop");
		
		WorkflowNode ret = new WorkflowNode(true,true,"testop"+count);
		ret.setAbstractOperator(aop);
		count++;
		return ret;
	}
	
	public static void createMaterializedOps(int inputs, int outputs, int matches,AbstractOperator checkOp) throws Exception{
		if(inputs==0)
			inputs=1;
		if(outputs==0)
			outputs=1;
		
		if(OperatorLibrary.getMatches(checkOp).size()>0)
			return;
		
		for (int i = 0; i < matches; i++) {
			Operator op = new Operator("testop_"+inputs+"_"+outputs+"_"+i, "");
			op.readPropertiesFromStream(new FileInputStream(new File("/local/npapa/asap-server/asapLibrary/operators/testop/description")));
			op.add("Constraints.Input.number", inputs+"");
			op.add("Constraints.Output.number", outputs+"");
			op.configureModel();	
			
			OperatorLibrary.add(op);
		}
	}
}
