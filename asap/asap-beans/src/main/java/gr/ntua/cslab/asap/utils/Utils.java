package gr.ntua.cslab.asap.utils;

import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Utils {
	public static boolean deleteDirectory(File dir) {
	    if(! dir.exists() || !dir.isDirectory())    {
	        return false;
	    }

	    String[] files = dir.list();
	    for(int i = 0, len = files.length; i < len; i++)    {
	        File f = new File(dir, files[i]);
	        if(f.isDirectory()) {
	            deleteDirectory(f);
	        }else   {
	            f.delete();
	        }
	    }
	    return dir.delete();
	}
	
	public static WorkflowDictionary unmarshall(String xmlFile) throws JAXBException {
		File file = new File(xmlFile);
		JAXBContext jaxbContext = JAXBContext.newInstance(WorkflowDictionary.class);
 
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		WorkflowDictionary d = (WorkflowDictionary) jaxbUnmarshaller.unmarshal(file);
		return d;
	}
	

	public static WorkflowDictionary unmarshall(InputStream stream) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(WorkflowDictionary.class);
 
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		WorkflowDictionary d = (WorkflowDictionary) jaxbUnmarshaller.unmarshal(stream);
		return d;
	}
	

	public static void marshall(WorkflowDictionary workflow, String file) throws Exception {
		File f = new File(file);
		
        OutputStream out = new FileOutputStream(f);
        JAXBContext jaxbContext = JAXBContext.newInstance(WorkflowDictionary.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		 
		jaxbMarshaller.marshal(workflow,out);
		out.close();
	}
}
