/*
 * Copyright 2016 ASAP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package gr.ntua.cslab.asap.daemon.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class YarnMetricsClient {

	private static final Log LOG = LogFactory.getLog( YarnMetricsClient.class);
	
	public YarnMetricsClient() {
        super();

	    }
	
	public static String issueRequestYarnClusterMetrics( YarnConfiguration conf) throws Exception {
		String masterDNS = conf.get( "yarn.resourcemanager.webapp.address");
		String urlString = "http://" + masterDNS + "/ws/v1/cluster/metrics";
		StringBuilder builder = null;
		StringBuffer xmlStr = null;
		InputStream in = null;
		ConcurrentHashMap< String, String> metrics = null;
		JAXBContext jaxbContext = JAXBContext.newInstance( HashMap.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setRequestMethod("GET");
			con.setRequestProperty("accept", "application/xml");
			con.setRequestProperty("Content-type", "application/xml");	        
			con.setDoInput(true);
		
			builder = new StringBuilder();
			in = con.getInputStream();
			byte[] buffer = new byte[1024];
			int count;
			while((count = in.read(buffer))!=-1) {
				builder.append(new String(buffer,0,count) + "\n");
			}
			xmlStr = new StringBuffer( builder.toString());
		} 
		catch (Exception e)
		{
				LOG.error( e.getStackTrace());
				e.printStackTrace();
		}
	    return builder.toString();
	}

	public static String issueRequestApplicationLogs( YarnConfiguration conf, String applicationId) throws Exception {
		String masterDNS = conf.get( "yarn.resourcemanager.webapp.address");
		String urlString = "http://" + masterDNS + "/ws/v1/cluster/apps/" + applicationId;
		StringBuilder builder = null;
		String xmlStr = null;
		InputStream in = null;
		HashMap< String, String> metrics = new HashMap();
		JAXBContext jaxbContext = JAXBContext.newInstance( HashMap.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		try {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
			con.setRequestMethod("GET");
			con.setRequestProperty("accept", "application/xml");
			con.setRequestProperty("Content-type", "application/xml");	        
			con.setDoInput(true);
		
			builder = new StringBuilder();
			in = con.getInputStream();
			byte[] buffer = new byte[1024];
			int count;
			while((count = in.read(buffer))!=-1) {
				builder.append(new String(buffer,0,count) + "\n");
			}
			String[] tmetrics = null;
			xmlStr = builder.toString();
			xmlStr = xmlStr.replaceAll( "<\\?[^>]+\\?>", "" );
                    	xmlStr = xmlStr.replaceAll( "<app>", "");
			xmlStr = xmlStr.replaceAll( "</[^>]+>", "\n");
			xmlStr = xmlStr.replaceAll( "[<]+", "");
			xmlStr = xmlStr.replaceAll( "[>]+", " ");
			tmetrics = xmlStr.split( "\n");
			//System.out.println( "LOGS ARE:\n" + xmlStr);
			//System.out.println( "METRICS\n");
			for( int i = 0; i < tmetrics.length; i++){
				//System.out.println( tmetrics[ i]);
				if( !tmetrics.equals( "") && tmetrics[ i].trim().indexOf( " ") > -1){
					metrics.put( tmetrics[ i].split(" ")[ 0], tmetrics[ i].split( " ")[ 1]);
				}
			}
			//System.out.println( "METRICS ARE: " + metrics);
		} 
		catch (Exception e)
		{
				LOG.error( e.getStackTrace());
				e.printStackTrace();
		}
	    return metrics.get( "amContainerLogs");
	}
}
