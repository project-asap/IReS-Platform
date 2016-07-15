package gr.ntua.cslab.asap.testMaterialization;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class GraphNode {
	private Job job;
	
	private Hashtable<Integer, GraphNode> children;
	
	private Hashtable<Integer, GraphNode> parents;

	private int layer;
	
	public Job getJob() {
		return job;
	}
	
	public void setJob(Job job) {
		this.job = job;
	}
	
	
	public Hashtable<Integer, GraphNode> getChildren() {
		return children;
	}

	public void setChildren(Hashtable<Integer, GraphNode> children) {
		this.children = children;
	}

	public Hashtable<Integer, GraphNode> getParents() {
		return parents;
	}

	public void setParents(Hashtable<Integer, GraphNode> parents) {
		this.parents = parents;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	@Override
	public String toString() {
		String temp =  "GraphNode [id = " + this.job.getJobID() + "layer=" + layer + "]";
		temp += "Parent nodes:\n";
		Enumeration<GraphNode> e = this.parents.elements();
		while (e.hasMoreElements()) {
			temp += e.nextElement().getJob().getJobID() + "\n";
		}
		temp += "Children nodes:\n";
		e = this.children.elements();
		while (e.hasMoreElements()) {
			temp += e.nextElement().getJob().getJobID() + "\n";
		}
		return temp;
	}
	
	
}
