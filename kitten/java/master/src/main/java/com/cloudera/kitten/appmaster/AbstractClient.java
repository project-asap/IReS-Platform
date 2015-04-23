package com.cloudera.kitten.appmaster;

import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractClient {

	private static final Log LOG = LogFactory.getLog(AbstractClient.class);
    /**
     * Issues a new Request and returns a string with the response - if  any.
     * @param requestType
     * @param document
     * @param input
     * @return
     * @throws MalformedURLException
     * @throws IOException 
     */
    public static String issueRequest(String id, WorkflowDictionary workflow) {
        String urlString = "http://master:80/runningWorkflows/report/"+id+"/";
        String ret="";
		try {
	        LOG.info("Issuing urlString: "+urlString);
			System.out.println("Issuing urlString: "+urlString);
	        URL url = new URL(urlString);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();
	        
	        con.setRequestMethod("POST");

	        con.setRequestProperty("accept", "application/octet-stream");
	        con.setRequestProperty("Content-type", "application/octet-stream");
	        con.setDoInput(true);
	        con.setDoOutput(true);
	        
	        OutputStream out = con.getOutputStream();
	        JAXBContext jaxbContext = JAXBContext.newInstance(WorkflowDictionary.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	 
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 
			jaxbMarshaller.marshal(workflow,out);
	        
	        
	        int responseCode = con.getResponseCode();
	        StringBuilder builder = new StringBuilder();
	        
	    	InputStream in = con.getInputStream();
	        byte[] buffer = new byte[1024];
	        int count;
	        while((count = in.read(buffer))!=-1) {
	            builder.append(new String(buffer,0,count));
	        }
	        ret = builder.toString();
	        System.out.println("Output: "+ret);
	        LOG.info("Output: "+ret);
		} catch (Exception e) {
			LOG.error(e.getStackTrace());
			e.printStackTrace();
		}
        return ret;
        
    }
}