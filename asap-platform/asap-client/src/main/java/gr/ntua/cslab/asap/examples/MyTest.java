package gr.ntua.cslab.asap.examples;

import java.io.PrintWriter;
import java.util.Random;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.OperatorClient;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

public class MyTest {
	public static void main(String[] args) throws Exception {
		
		ClientConfiguration conf = new ClientConfiguration("master", 1323);
		OperatorClient cli = new OperatorClient();
		cli.setConfiguration(conf);
		AbstractOperator aop = new AbstractOperator("PageRank");
		
		aop.add("Constraints.Input.number","1");
		aop.add("Constraints.OpSpecification.Algorithm.name", "pagerank");
		aop.add("Constraints.Engine", "Java");
		aop.add("Constraints.Output.number", "1");

		cli.addAbstractOperator(aop);
		
		WorkflowClient wcli = new WorkflowClient();
		wcli.setConfiguration(conf);
		
		wcli.removeAbstractWorkflow("pagerank");
		
		AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("pagerank");
		Dataset d1 = new Dataset("hdfs_file");

		WorkflowNode t1 = new WorkflowNode(false,false,"hdfs_file");
		t1.setDataset(d1);

		AbstractOperator abstractOp = new AbstractOperator("PageRank");
		WorkflowNode op1 = new WorkflowNode(true,true,"PageRank");
		op1.setAbstractOperator(abstractOp);
		
		Dataset d2 = new Dataset("d1");
		WorkflowNode t2 = new WorkflowNode(false,true,"d1");
		t2.setDataset(d2);
		
		op1.addInput(0,t1);
		op1.addOutput(0, t2);
		
		t2.addInput(0,op1);
		
		abstractWorkflow.addTarget(t2);
		
		wcli.addAbstractWorkflow(abstractWorkflow);
		//cli.removeAbstractWorkflow("abstractTest1");
		
		String policy ="metrics,cost,execTime\n"+
						"groupInputs,execTime,max\n"+
						"groupInputs,cost,sum\n"+
						"function,execTime,min";
		

		String[] nodes = {"1000", "10000", "100000", "1000000"};//, "10000000"};
		String[] avgDeg = {"10", "50", "100"};
		String[] iterations = {"10", "20", "30", "40", "50", "60", "70" ,"80", "90", "100"};
		String[] memory = {"512", "1024", "2048", "3072","4096", "5120", "6144"};
		
		int count=0;
		Random r = new Random();
		double abs_error=0, rel_error=0,abs_error_sum=0, rel_error_sum=0;
		PrintWriter writer = new PrintWriter("resultsNew.txt", "UTF-8");
		while(true){
			String node = nodes[r.nextInt(nodes.length)];
			String aD = avgDeg[r.nextInt(avgDeg.length)];
			String mem = memory[r.nextInt(memory.length)];
			String it = iterations[r.nextInt(iterations.length)];
			
			if(Integer.parseInt(node)*Integer.parseInt(aD)>10000000)
				continue;
			writer.println("V: "+node+" E: "+aD+" mem: "+mem+" it:"+it);
			writer.flush();
			
			String parameters ="hdfs_file.Constraints.type = graph\n"+
					"hdfs_file.Execution.path = hdfs://master:9000/user/root/randomGraphs/V"+node+"_E"+aD+"\n"+
					"PageRank_Java.Optimization.iterations="+it+".0\n"+
					"PageRank_Java.SelectedParam.iterations="+it+".0\n"+
					"PageRank_Java.Optimization.cores=1.0\n"+
					"PageRank_Java.SelectedParam.cores=1.0\n"+
					"PageRank_Java.Optimization.memory="+mem+".0\n"+
					"PageRank_Java.SelectedParam.memory="+mem+".0\n"+
					"hdfs_file.Optimization.nodes = "+node+"\n"+
					"hdfs_file.Optimization.avgDegree = "+aD+"\n";
			
			String materializedWorkflow = wcli.materializeWorkflowWithParameters("pagerank", policy,parameters);
			System.out.println(materializedWorkflow);

			double estimatedTime = Double.parseDouble(wcli.getMaterializedWorkflowDescription(materializedWorkflow).getOperator("PageRank_Java_0").getExecTime());
			String w = wcli.executeWorkflow(materializedWorkflow);
			long start = System.currentTimeMillis();
			wcli.waitForCompletion(w);
			long stop = System.currentTimeMillis();
			double actualTime = (double)(stop-start)/1000.0 -12.0;
			writer.println("Estimated: "+estimatedTime+" Actual: " +actualTime);
			writer.flush();
			
			abs_error=Math.abs(estimatedTime-actualTime);
			rel_error=Math.abs(estimatedTime-actualTime)/Math.abs(actualTime);
			count++;
			abs_error_sum+=abs_error;
			rel_error_sum+=rel_error;
			writer.println("Step: "+count+" abs_error: "+abs_error+" rel_error: "+rel_error);
			writer.println("Step: "+count+" abs_error_total: "+abs_error_sum/(double)count+" rel_error_total: "+rel_error_sum/(double)count);
			writer.flush();
			if(count>=1000)
				break;
			//Thread.sleep(100000);
		}
		writer.close();
	}
}
