package gr.ntua.cslab.asap.rest.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowDictionary {
	List<OperatorDictionary> operators;

	public WorkflowDictionary() {
		operators = new ArrayList<OperatorDictionary>();
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

	public void setOutputsRunning(String name) {
		
		for(OperatorDictionary op: operators){
			if(op.getIsOperator().equals("false") && op.getStatus().equals("warn")){
				for(String op1 : op.getInput()){
					if(op1.equals(name) ){
						op.setStatus("running");
						setOutputsRunning(op.getName());
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
	
}
