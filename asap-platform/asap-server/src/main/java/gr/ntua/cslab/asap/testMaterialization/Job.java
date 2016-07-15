package gr.ntua.cslab.asap.testMaterialization;

import java.util.Enumeration;
import java.util.Hashtable;

public class Job {
	private int jobID;
	private Hashtable<String, UsedFile> inputs;
	private Hashtable<String, UsedFile> outputs;
	private double execTime;
	
	public Hashtable<String, UsedFile> getInputs() {
		return inputs;
	}

	public void setInputs(Hashtable<String, UsedFile> inputs) {
		this.inputs = inputs;
	}

	public Hashtable<String, UsedFile> getOutputs() {
		return outputs;
	}

	public void setOutputs(Hashtable<String, UsedFile> outputs) {
		this.outputs = outputs;
	}

	public int getJobID() {
		return jobID;
	}
	
	public void setJobID(int jobID) {
		this.jobID = jobID;
	}
	
	public double getExecTime() {
		return execTime;
	}
	
	public void setExecTime(double execTime) {
		this.execTime = execTime;
	}

	@Override
	public String toString() {
		String temp =  "Job [jobID=" + jobID + ", execTime=" + execTime + "]\nInput Files:\n";
		Enumeration<UsedFile> einputs = this.inputs.elements();
		while (einputs.hasMoreElements()) {
			temp += einputs.nextElement().toString() + "\n";
		}
		temp += "Output Files:\n";
		Enumeration<UsedFile> eoutputs = this.outputs.elements();
		while (eoutputs.hasMoreElements()) {
			temp += eoutputs.nextElement().toString() + "\n";
		}
		return temp;
	}
	
	

}
