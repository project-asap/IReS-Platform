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


package gr.ntua.cslab.asap.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.OperatorClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;


public class Tests {
	
	@BeforeClass
	public static void setup() throws InterruptedException, IOException {
		Runtime.getRuntime().exec("/Users/npapa/Documents/workspaceASAP/IReS-Platform/asap-platform/asap-server/src/main/scripts/asap-server start");
		Thread.sleep(5000);
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		Runtime.getRuntime().exec("/Users/npapa/Documents/workspaceASAP/IReS-Platform/asap-platform/asap-server/src/main/scripts/asap-server stop");
	}
	  
	@Test
	public void testCreateOperator() throws Exception {
		Operator op = new Operator("TestOp","");
		
		op.add("Constraints.EngineSpecification.Centralized", "WEKA1");
		op.add("Constraints.Input.number","1");
		op.add("Constraints.Input0.Engine.FS", "HDFS");
		op.add("Constraints.Input0.type", "arff");
		op.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		op.add("Constraints.Output.number", "1");
		op.add("Constraints.Output0.Engine.FS", "HDFS");
		op.add("Optimization.cost", "1.0");
		op.add("Optimization.inputSpace.In0.points", "Double,1.0,5000.0,500.0");
		op.add("Optimization.inputSpace.k", "Double,1.0,21.0,5.0");
		op.add("Optimization.model.cost", "gr.ntua.ece.cslab.panic.core.models.UserFunction");
		op.add("Optimization.model.execTime","gr.ntua.ece.cslab.panic.core.models.AbstractWekaModel");
		op.add("Optimization.outputSpace.cost", "Double");
		op.add("Optimization.outputSpace.execTime","Double");
		

	    assertEquals("WEKA1", op.getParameter("Constraints.EngineSpecification.Centralized"));
	    assertEquals("1.0", op.getParameter("Optimization.cost"));
	    assertEquals("1", op.getParameter("Constraints.Input.number"));
	}
	
	@Test
	public void testPutOperator() throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		OperatorClient cli = new OperatorClient();
		cli.setConfiguration(conf);
		Operator op = new Operator("TestOp","");
		
		op.add("Constraints.EngineSpecification.Centralized", "WEKA1");
		op.add("Constraints.Input.number","1");
		op.add("Constraints.Input0.Engine.FS", "HDFS");
		op.add("Constraints.Input0.type", "arff");
		op.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		op.add("Constraints.Output.number", "1");
		op.add("Constraints.Output0.Engine.FS", "HDFS");
		op.add("Optimization.cost", "1.0");
		op.add("Optimization.inputSpace.In0.points", "Double,1.0,5000.0,500.0");
		op.add("Optimization.inputSpace.k", "Double,1.0,21.0,5.0");
		op.add("Optimization.model.cost", "gr.ntua.ece.cslab.panic.core.models.UserFunction");
		op.add("Optimization.model.execTime","gr.ntua.ece.cslab.panic.core.models.AbstractWekaModel");
		op.add("Optimization.outputSpace.cost", "Double");
		op.add("Optimization.outputSpace.execTime","Double");

		cli.addOperator(op);
	}
	
	@Test
	public void testRemoveOperator() throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		OperatorClient cli = new OperatorClient();
		cli.setConfiguration(conf);

		Operator op = new Operator("TestOp","");
		
		op.add("Constraints.EngineSpecification.Centralized", "WEKA1");
		op.add("Constraints.Input.number","1");
		op.add("Constraints.Input0.Engine.FS", "HDFS");
		op.add("Constraints.Input0.type", "arff");
		op.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		op.add("Constraints.Output.number", "1");
		op.add("Constraints.Output0.Engine.FS", "HDFS");
		op.add("Optimization.cost", "1.0");
		op.add("Optimization.inputSpace.In0.points", "Double,1.0,5000.0,500.0");
		op.add("Optimization.inputSpace.k", "Double,1.0,21.0,5.0");
		op.add("Optimization.model.cost", "gr.ntua.ece.cslab.panic.core.models.UserFunction");
		op.add("Optimization.model.execTime","gr.ntua.ece.cslab.panic.core.models.AbstractWekaModel");
		op.add("Optimization.outputSpace.cost", "Double");
		op.add("Optimization.outputSpace.execTime","Double");

		cli.addOperator(op);
		cli.removeOperator("TestOp");
	}
	

	@Test
	public void testPutAndMatchOperator() throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		OperatorClient cli = new OperatorClient();
		cli.setConfiguration(conf);
		Operator op = new Operator("TestOp","");
		
		op.add("Constraints.EngineSpecification.Centralized", "WEKA1");
		op.add("Constraints.Input.number","1");
		op.add("Constraints.Input0.Engine.FS", "HDFS");
		op.add("Constraints.Input0.type", "arff");
		op.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		op.add("Constraints.Output.number", "1");
		op.add("Constraints.Output0.Engine.FS", "HDFS");
		op.add("Optimization.cost", "1.0");
		op.add("Optimization.inputSpace.In0.points", "Double,1.0,5000.0,500.0");
		op.add("Optimization.inputSpace.k", "Double,1.0,21.0,5.0");
		op.add("Optimization.model.cost", "gr.ntua.ece.cslab.panic.core.models.UserFunction");
		op.add("Optimization.model.execTime","gr.ntua.ece.cslab.panic.core.models.AbstractWekaModel");
		op.add("Optimization.outputSpace.cost", "Double");
		op.add("Optimization.outputSpace.execTime","Double");

		cli.addOperator(op);

		AbstractOperator aop = new AbstractOperator("AbstrOp");
		
		aop.add("Constraints.Input.number","1");
		aop.add("Constraints.EngineSpecification.Centralized", "WEKA1");
		aop.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		aop.add("Constraints.Output.number", "1");

		cli.addAbstractOperator(aop);
		
		String[] ops = cli.checkMatches(aop);
		
	    assertEquals(true, ops.length>=1);
	    assertEquals("TestOp", ops[0]);
		cli.removeOperator("TestOp");
		cli.removeAbstractOperator("AbstrOp");
	}
	

	@Test
	public void testAddAbstractWorkflow() throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		WorkflowClient cli = new WorkflowClient();
		cli.setConfiguration(conf);
		
		cli.removeAbstractWorkflow("abstractTest1");
		
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
		op1.addOutput(0, t2);
		
		t2.addInput(0,op1);
		
		op2.addInput(0,t2);
		op2.addOutput(0,t3);
		
		t3.addInput(0,op2);
		abstractWorkflow.addTarget(t3);
		
		cli.addAbstractWorkflow(abstractWorkflow);
	}
	
	@Test
	public void testMaterializeAbstractWorkflow() throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		WorkflowClient cli = new WorkflowClient();
		cli.setConfiguration(conf);
		
		String policy ="metrics,cost,execTime\n"+
						"groupInputs,execTime,max\n"+
						"groupInputs,cost,sum\n"+
						"function,execTime,min";
		
		String materializedWorkflow = cli.materializeWorkflow("abstractTest1", policy);
	}
	
}
