package com.cloudera.kitten.appmaster;

import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class AbstractClient {

	private static final Log LOG = LogFactory.getLog(AbstractClient.class);
    /**
     * Issues a new Request and returns a string with the response - if  any.
     * @param conf
     * @param requestType
     * @param document
     * @param input
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String issueRequest(YarnConfiguration conf, String id, WorkflowDictionary workflow) {
    	String masterDNS = conf.get("yarn.resourcemanager.address").split(":")[0];
        String urlString = "http://"+masterDNS+":1323/runningWorkflows/report/"+id+"/";
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
     /**
     * Issues a new Request and returns a string with the response - if  any.
     * @param conf
     */
    public static String issueRequestClusterStatus( YarnConfiguration conf) {
    	String masterDNS = conf.get( "yarn.resourcemanager.address").split(":")[0];
        String urlString = "http://" + masterDNS + ":1323/clusterStatus";
        String services_n_status = "";
		try {
	        LOG.info("ClusterStatus Issuing urlString: " + urlString);
			//System.out.println("ClusterStatus Issuing urlString: " + urlString);
	        URL url = new URL(urlString);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();

	        con.setRequestMethod("GET");

	        con.setRequestProperty("accept", "text/html");
	        con.setRequestProperty("Content-type", "text/html");
	        con.setDoInput(true);
	        
	        StringBuilder builder = new StringBuilder();

	    	InputStream in = con.getInputStream();
	        byte[] buffer = new byte[1024];
	        int count;
	        while((count = in.read(buffer))!=-1) {
	            builder.append(new String(buffer,0,count));
	        }
	        services_n_status = builder.toString();
            //clean html response from its tags and replace them by a "_"
            services_n_status = services_n_status.replaceAll( "<[^>]+>", "_");
            //due to starting and closing tags, the tokens of the remainder text will be
            //separated by a double "_" i.e. "__" that must be trimmed
            //remove leading and trailing double "_" and substitute the intermediate by a newline
            services_n_status = services_n_status.replaceAll( "^__", "").replaceAll( "__$", "").replaceAll( "__", "\n");
	        LOG.info("Request response: " + services_n_status);
		} catch (Exception e) {
			LOG.error( e.getStackTrace());
			e.printStackTrace();
		}
        return services_n_status;
    }
    /**
    * Issues a new Request and returns a string with the response - if  any.
    * @param conf
    * @param id
    */
   public static void issueRequestReplan( YarnConfiguration conf, String id) {
   	String masterDNS = conf.get( "yarn.resourcemanager.address").split(":")[0];
       String urlString = "http://" + masterDNS + ":1323/web/runningWorkflows/replan/";
       String response = "";
		try {
	        LOG.info("Replanning workflow " + id + " Issuing urlString: " + urlString);
			//System.out.println("ClusterStatus Issuing urlString: " + urlString);
	        URL url = new URL(urlString);
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();

	        con.setRequestMethod("GET");

	        con.setRequestProperty("accept", "text/html");
	        con.setRequestProperty("Content-type", "text/html");
	        con.setDoInput(true);
	        
	        StringBuilder builder = new StringBuilder();

	    	InputStream in = con.getInputStream();
	        byte[] buffer = new byte[1024];
	        int count;
	        while((count = in.read(buffer))!=-1) {
	            builder.append(new String(buffer,0,count));
	        }
	        response = builder.toString();
            //clean html response from its tags and replace them by a "_"
	        response = response.replaceAll( "<[^>]+>", "_");
            //due to starting and closing tags, the tokens of the remainder text will be
            //separated by a double "_" i.e. "__" that must be trimmed
            //remove leading and trailing double "_" and substitute the intermediate by a newline
	        response = response.replaceAll( "^__", "").replaceAll( "__$", "").replaceAll( "__", "\n");
	        LOG.info("Request response: " + response);
		} catch (Exception e) {
			LOG.error( e.getStackTrace());
			e.printStackTrace();
		}
       return;
   }    
}
