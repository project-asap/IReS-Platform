package gr.ntua.cslab.asap.operators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaterializedOperators {
	private List<Operator> operators;
	
	
	
	public MaterializedOperators() {
		operators = new ArrayList<Operator>();

		Operator op = new Operator("HBase_HashJoin", "/tmp");
		op.add("Constraints.Input.number","2");
		op.add("Constraints.Output.number","1");
		op.add("Constraints.Input0.DataInfo.Attributes.number","2");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op.add("Constraints.Input1.DataInfo.Attributes.number","2");
		op.add("Constraints.Input1.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op.add("Constraints.Output0.DataInfo.Attributes.number","2");
		op.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		op.add("Constraints.OpSpecification.Algorithm.Join.JoinCondition","in1.atr1 = in2.atr2");
		op.add("Constraints.OpSpecification.Algorithm.Join.type", "HashJoin");

		op.add("Constraints.EngineSpecification.Distributed.MapReduce.masterLocation", "127.0.0.1");
		op.add("Optimization.execTime", "100.0");
		//op.add("Properties.MaintainTags", ".*");

		Operator op1 = new Operator("Java_SortMergeJoin", "/tmp");
		op1.add("Constraints.Input.number","2");
		op1.add("Constraints.Output.number","1");
		op1.add("Constraints.Input0.DataInfo.Attributes.number","2");
		op1.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op1.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op1.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		op1.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		op1.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		op1.add("Constraints.Input1.DataInfo.Attributes.number","2");
		op1.add("Constraints.Input1.DataInfo.Attributes.Atr1.type","Varchar");
		op1.add("Constraints.Input1.DataInfo.Attributes.Atr2.type","Varchar");
		op1.add("Constraints.Input1.Engine.DB.Relational.MySQL.schema","...");
		op1.add("Constraints.Input1.Engine.DB.Relational.MySQL.location","127.0.0.1");


		op1.add("Constraints.Output0.DataInfo.Attributes.number","2");
		op1.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op1.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op1.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
		op1.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
		op1.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		op1.add("Constraints.OpSpecification.Algorithm.Join.JoinCondition","in1.atr1 = in2.atr2");
		op1.add("Constraints.OpSpecification.Algorithm.Join.type", "SortMergeJoin");
		
		op1.add("Constraints.EngineSpecification.Centralized.Java.location", "127.0.0.1");
		op1.add("Optimization.execTime", "1000.0");
		
		Operator sort = new Operator("Sort", "/tmp");
		sort.add("Constraints.Input.number","1");
		sort.add("Constraints.Output.number","1");
		sort.add("Constraints.Input0.DataInfo.Attributes.number","2");
		sort.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		sort.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		sort.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		sort.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		sort.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		sort.add("Constraints.Output0.DataInfo.Attributes.number","2");
		sort.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
		sort.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		sort.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
		sort.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
		sort.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		sort.add("Constraints.OpSpecification.Algorithm.Sort.sortingOrder","in1.atr1");
		sort.add("Constraints.OpSpecification.Algorithm.Sort.type", "ExternalSort");
		sort.add("Constraints.EngineSpecification.Distributed.MapReduce.masterLocation", "127.0.0.1");
		sort.add("Optimization.execTime", "10.0");
		

		Operator sort2 = new Operator("Sort2", "/tmp");
		sort2.add("Constraints.Input.number","1");
		sort2.add("Constraints.Output.number","1");
		sort2.add("Constraints.Input0.DataInfo.Attributes.number","2");
		sort2.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		sort2.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		sort2.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		sort2.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		sort2.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		sort2.add("Constraints.Output0.DataInfo.Attributes.number","2");
		sort2.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
		sort2.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		sort2.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
		sort2.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
		sort2.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		sort2.add("Constraints.OpSpecification.Algorithm.Sort.sortingOrder","in1.atr1");
		sort2.add("Constraints.OpSpecification.Algorithm.Sort.type", "ExternalSort");
		sort2.add("Constraints.EngineSpecification.Distributed.MapReduce.masterLocation", "127.0.0.1");
		sort2.add("Optimization.execTime", "100.0");

		Operator move = new Operator("Move_MySQL_HBase", "/tmp");
		move.add("Constraints.Input.number","1");
		move.add("Constraints.Output.number","1");
		move.add("Constraints.Input0.DataInfo.Attributes.number","2");
		move.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","Varchar");
		move.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","Varchar");
		move.add("Constraints.Input0.Engine.DB.Relational.MySQL.schema","...");
		move.add("Constraints.Input0.Engine.DB.Relational.MySQL.location","127.0.0.1");

		move.add("Constraints.Output0.DataInfo.Attributes.number","2");
		move.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
		move.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		move.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
		move.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
		move.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		move.add("Constraints.OpSpecification.Algorithm.Move","");
		move.add("Constraints.EngineSpecification.Centralized.Java.location", "127.0.0.1");
		move.add("Optimization.execTime", "100.0");
		
		operators.add(op);
		operators.add(op1);
		operators.add(sort);
		operators.add(sort2);
		operators.add(move);
		
		
	}

	public static void main(String[] args) {
		new MaterializedOperators();
	}

	public List<Operator> getMatches(AbstractOperator abstractOperator){
		List<Operator> ret = new ArrayList<Operator>();
		for(Operator op : operators){
			if(abstractOperator.checkMatch(op))
				ret.add(op);
		}
		return ret;
	}

	public List<Operator> checkMove(Dataset from, Dataset to) {
		AbstractOperator abstractMove = new AbstractOperator("move");
		abstractMove.moveOperator(from,to);
		return getMatches(abstractMove);
	}
}
