package gr.ntua.cslab.asap.client;

import java.io.IOException;
import java.net.MalformedURLException;

import gr.ntua.cslab.asap.operators.Operator;

public class Test {
	public static void main(String[] args) throws MalformedURLException, IOException {
		ClientConfiguration conf = new ClientConfiguration("master", 1324);
		RestClient cli = new RestClient();
		
		Operator op = new Operator("TestOp","");
		op.add("Constraints.Input.number","2");
		op.add("Constraints.Output.number","1");
		op.add("Constraints.Input0.DataInfo.Attributes.number","2");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		
		cli.issueRequest("GET", "?opname="+op.opName+"&?opString="+op.toKeyValues("\n"), null);
	}
}
