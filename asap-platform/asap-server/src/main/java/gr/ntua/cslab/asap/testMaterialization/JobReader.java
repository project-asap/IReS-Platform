package gr.ntua.cslab.asap.testMaterialization;

import java.util.ArrayList;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JobReader implements XMLReader {

	private Document doc;
	
	public JobReader(Document doc) {
		this.doc = doc;
	}
	
	@Override
	public Hashtable<Integer, Object> readData() {
		
		Hashtable<Integer, Object> nodes = new Hashtable<Integer, Object>();
		NodeList nList = doc.getElementsByTagName("job");
		for (int i = 0; i < nList.getLength(); i++) {
			Element nNode = (Element) nList.item(i);
			Job aJob = new Job();
			aJob.setJobID(Integer.parseInt(nNode.getAttribute("id").substring(2)));
			aJob.setExecTime(Double.parseDouble(nNode.getAttribute("runtime")));
			aJob.setInputs(new Hashtable<String, UsedFile>());
			aJob.setOutputs(new Hashtable<String, UsedFile>());
			NodeList files = nNode.getElementsByTagName("uses");
			if (files != null) {
				for (int j = 0; j < files.getLength(); j++) {
					Element file = (Element) files.item(j);
					UsedFile aFile = new UsedFile();
					aFile.setFilename(file.getAttribute("file"));
					aFile.setSize(Long.parseLong(file.getAttribute("size")));
					if (file.getAttribute("link").equals("input")) {
						aFile.setOutputfile(false);
						aJob.getInputs().put(aFile.getFilename(), aFile);
					}
					else {
						aFile.setOutputfile(true);
						aJob.getOutputs().put(aFile.getFilename(), aFile);
					}
				}
			}
			GraphNode aNode = new GraphNode();
			aNode.setChildren(new Hashtable<Integer, GraphNode>());
			aNode.setParents(new Hashtable<Integer, GraphNode>());
			aNode.setLayer(0);
			aNode.setJob(aJob);
			nodes.put(aJob.getJobID(), aNode);
		}
		
		return nodes;
	
	}

	@Override
	public Hashtable<Integer, Object> readData(Hashtable<Integer, Object> nodes) {
		return null;
	}
	
}
