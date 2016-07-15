package gr.ntua.cslab.asap.testMaterialization;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLReaderFactory {

	private Document doc;
	
	public XMLReaderFactory(String xmlfile) throws ParserConfigurationException, SAXException, IOException {
		super();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		this.doc = dBuilder.parse(xmlfile);
		this.doc.getDocumentElement().normalize();
	}
	
	public XMLReader getReader(String type) {
		if (type.equals("job")) {
			return new JobReader(this.doc);
		}
		else if (type.equals("graphnode")) {
			return new NodeReader(this.doc);
		}
		else {
			return null;
		}
	}
	
}
