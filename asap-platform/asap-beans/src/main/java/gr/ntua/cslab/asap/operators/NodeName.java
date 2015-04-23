package gr.ntua.cslab.asap.operators;

public class NodeName {
	public String name;
	public NodeName nextName;
	public boolean isRegex;
	
	public NodeName(String name, NodeName nextName, boolean isRegex) {
		this.name = name;
		this.nextName = nextName;
		this.isRegex = isRegex;
	}
	
	
}
