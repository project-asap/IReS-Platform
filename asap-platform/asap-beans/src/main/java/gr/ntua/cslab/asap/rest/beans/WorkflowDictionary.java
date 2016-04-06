package gr.ntua.cslab.asap.rest.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowDictionary {
	List<OperatorDictionary> operators;
	private String name = null;

	public WorkflowDictionary() {
		this( "");
	}
	
	public WorkflowDictionary( String name) {
		operators = new ArrayList<OperatorDictionary>();
		this.name = name;
	}	

	public void addOperator(OperatorDictionary op){
		operators.add(op);
	}
	
	public List<OperatorDictionary> getOperators() {
		return operators;
	}

	public void setOperators(List<OperatorDictionary> operators) {
		this.operators = operators;
	}

	public OperatorDictionary getOperator(String name) {
		for(OperatorDictionary op: operators){
			if(op.getName().equals(name))
				return op;
		}
		return null;
	}

	public void setOutputsRunning(String name, String status) {
		if( status == null){
			status = "running";
		}
		for(OperatorDictionary op: operators){
			if(op.getIsOperator().equals("false") && ( op.getStatus().equals("warn") || op.getStatus().equals("running"))){
				for(String op1 : op.getInput()){
					if(op1.equals(name) ){
						op.setStatus( status);
						setOutputsRunning(op.getName(), status);
					}
				}
			}
		}
	}

	public void replaceDescription(String target, String replacement) {
		for(OperatorDictionary op : operators){
			op.setDescription(op.getDescription().replace(target, replacement));
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName( String name){
		this.name = name;
	}	
}
