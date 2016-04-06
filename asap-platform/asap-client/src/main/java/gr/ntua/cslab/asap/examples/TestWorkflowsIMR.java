package gr.ntua.cslab.asap.examples;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.OperatorClient;
import gr.ntua.cslab.asap.client.RestClient;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.NodeName;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

public class TestWorkflowsIMR {
	
	public static void main(String[] args) throws Exception {

		ClientConfiguration conf = new ClientConfiguration("asap-master", 1323);
		WorkflowClient cli = new WorkflowClient();
		cli.setConfiguration(conf);
//		
//		WorkflowDictionary wd = cli.getMaterializedWorkflowDescription("abstractTest1_2016_02_25_12:01:56");
//		for(OperatorDictionary op : wd.getOperators()){
//			if(op.getIsOperator().equals("true"))
//				System.out.println(op.getNameNoID()+" "+op.getCost());
//		}
//		
		cli.removeAbstractWorkflow("abstractTest1");
		AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("abstractTest1");
		
		Dataset d1 = new Dataset("IMR_docs");
		WorkflowNode t1 = new WorkflowNode(false,false,"IMR_docs");
		t1.setDataset(d1);
		

		AbstractOperator abstractOp = new AbstractOperator("w2v_train");
		WorkflowNode op1 = new WorkflowNode(true,true,"w2v_train");
		op1.setAbstractOperator(abstractOp);
		//abstractOp.writeToPropertiesFile(abstractOp.opName);

		AbstractOperator abstractOp1 = new AbstractOperator("w2v_vectorize");
		WorkflowNode op2 = new WorkflowNode(true,true,"w2v_vectorize");
		op2.setAbstractOperator(abstractOp1);
		
		AbstractOperator abstractOp2 = new AbstractOperator("lr_train");
		WorkflowNode op3 = new WorkflowNode(true,true,"lr_train");
		op3.setAbstractOperator(abstractOp2);
		
		AbstractOperator abstractOp3 = new AbstractOperator("lr_classify");
		WorkflowNode op4 = new WorkflowNode(true,true,"lr_classify");
		op4.setAbstractOperator(abstractOp3);
		//abstractOp1.writeToPropertiesFile(abstractOp1.opName);
		
		Dataset d2 = new Dataset("d2");
		WorkflowNode t2 = new WorkflowNode(false,true,"d2");
		t2.setDataset(d2);
		
		Dataset d3 = new Dataset("d3");
		WorkflowNode t3 = new WorkflowNode(false,true,"d3");
		t3.setDataset(d3);
		
		Dataset d4 = new Dataset("d4");
		WorkflowNode t4 = new WorkflowNode(false,true,"d4");
		t4.setDataset(d4);
		

		Dataset d5 = new Dataset("d5");
		WorkflowNode t5 = new WorkflowNode(false,true,"d5");
		t5.setDataset(d5);
		
		
		op1.addInput(0,t1);
		op1.addOutput(0, t2);
		
		op2.addInput(0,t1);
		op2.addInput(1,t2);
		op2.addOutput(0, t3);

		op3.addInput(0,t3);
		op3.addOutput(0, t4);
		
		op4.addInput(0,t3);
		op4.addInput(1,t4);
		op4.addOutput(0, t5);

		t2.addInput(0,op1);
		t3.addInput(0,op2);
		t4.addInput(0,op3);
		t5.addInput(0,op4);
		
		abstractWorkflow.addTarget(t5);
		
		cli.addAbstractWorkflow(abstractWorkflow);
		//cli.removeAbstractWorkflow("abstractTest1");
		
		String policy ="metrics,cost,execTime\n"+
						"groupInputs,execTime,max\n"+
						"groupInputs,cost,sum\n"+
						"function,2*execTime+3*cost,min";
		
		String materializedWorkflow = cli.materializeWorkflow("abstractTest1", policy);
//		System.out.println(materializedWorkflow);
//		cli.executeWorkflow(materializedWorkflow);
		
//		cli.removeMaterializedWorkflow("abstractTest1_2015_11_30_13:46:59");
		
	}
}
