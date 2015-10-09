package gr.ntua.cslab.asap.examples;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import javax.xml.bind.JAXB;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.OperatorClient;
import gr.ntua.cslab.asap.client.RestClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;

public class TestOperators {
	public static void main(String[] args) throws Exception {
		ClientConfiguration conf = new ClientConfiguration("localhost", 1323);
		OperatorClient cli = new OperatorClient();
		cli.setConfiguration(conf);
		
		Operator op = new Operator("TestOp","");
		
		op.add("Constraints.EngineSpecification.Centralized", "WEKA");
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
		aop.add("Constraints.OpSpecification.Algorithm.name", "k-means");
		aop.add("Constraints.Output.number", "1");

		cli.addAbstractOperator(aop);
		
		String[] ops = cli.checkMatches(aop);
		for (int i = 0; i < ops.length; i++) {
			System.out.println(ops[i]);
		}
		cli.removeOperator("TestOp");
		cli.removeAbstractOperator("AbstrOp");
	}
}
