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

public class TestWorkflows {
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
		AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("abstractTest1");
		Dataset d1 = new Dataset("crawlDocuments");

		WorkflowNode t1 = new WorkflowNode(false,false,"crawlDocuments");
		t1.setDataset(d1);

		AbstractOperator abstractOp = new AbstractOperator("TF_IDF");
		WorkflowNode op1 = new WorkflowNode(true,true,"TF_IDF");
		op1.setAbstractOperator(abstractOp);
		//abstractOp.writeToPropertiesFile(abstractOp.opName);

		AbstractOperator abstractOp1 = new AbstractOperator("k-Means");
		WorkflowNode op2 = new WorkflowNode(true,true,"k-Means");
		op2.setAbstractOperator(abstractOp1);
		//abstractOp1.writeToPropertiesFile(abstractOp1.opName);
		
		Dataset d2 = new Dataset("d2");
		WorkflowNode t2 = new WorkflowNode(false,true,"d2");
		t2.setDataset(d2);
		
		Dataset d3 = new Dataset("d3");
		WorkflowNode t3 = new WorkflowNode(false,true,"d3");
		t3.setDataset(d3);
		
		op1.addInput(0,t1);
		t2.addInput(0,op1);
		op2.addInput(0,t2);
		t3.addInput(0,op2);
		abstractWorkflow.addTarget(t3);
		
		cli.addAbstractWorkflow(abstractWorkflow);
		//cli.removeAbstractWorkflow("abstractTest1");
		
		String policy ="metrics,cost,execTime\n"+
						"groupInputs,execTime,max\n"+
						"groupInputs,cost,sum\n"+
						"function,2*execTime+3*cost,min";
		
		String materializedWorkflow = cli.materializeWorkflow("abstractTest1", policy);
		System.out.println(materializedWorkflow);
		cli.executeWorkflow(materializedWorkflow);
		
//		cli.removeMaterializedWorkflow("abstractTest1_2015_11_30_13:46:59");
		
	}
}
