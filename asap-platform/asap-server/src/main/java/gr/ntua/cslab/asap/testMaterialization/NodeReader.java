package gr.ntua.cslab.asap.testMaterialization;

import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class NodeReader implements XMLReader {

	private Document doc;
	
	public NodeReader(Document doc) {
		this.doc = doc;
	}

	@Override
	public Hashtable<Integer, Object> readData(Hashtable<Integer, Object> nodes) {
		
		NodeList nList = doc.getElementsByTagName("child");
		for (int i = 0; i < nList.getLength(); i++) {
			Element xNode = (Element) nList.item(i); 
			GraphNode aNode = (GraphNode) nodes.get(Integer.parseInt(xNode.getAttribute("ref").substring(2)));
			int maxlayer = 0;
			NodeList parents = xNode.getElementsByTagName("parent");
			for (int j = 0; j < parents.getLength(); j++) {
				Element xParent = (Element) parents.item(j);
				GraphNode aParent = (GraphNode) nodes.get(Integer.parseInt(xParent.getAttribute("ref").substring(2)));
				maxlayer = (aParent.getLayer() > maxlayer) ? aParent.getLayer() : maxlayer;
				aNode.getParents().put(aParent.getJob().getJobID(), aParent);
				aParent.getChildren().put(aNode.getJob().getJobID(), aNode);
			}
			aNode.setLayer(++maxlayer);
		}
		
		return nodes;
	}

	@Override
	public Hashtable<Integer, Object> readData() {
		// TODO Auto-generated method stub
		return null;
	}
}
