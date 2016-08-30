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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class YarnMetricsClient {

	private static final Log LOG = LogFactory.getLog( YarnMetricsClient.class);
	
	public YarnMetricsClient() {
        super();

	    }

	/**
  	 * Returns a string having all the dynamic metrics of YARN cluster
  	 * 
  	 * @author Vassilis Papaioannou
  	 * @parm conf		YARN cluster configuration
  	 * @return metrics 	the dynamic metrics
  	 */
	public static String issueRequestYarnClusterMetrics( YarnConfiguration conf) throws Exception {
		String masterDNS = conf.get( "yarn.resourcemanager.webapp.address");
		String urlString = "http://" + masterDNS + "/ws/v1/cluster/metrics";
		StringBuilder builder = null;
		InputStream in = null;
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
		} 
		catch (Exception e)
		{
				LOG.error( e.getStackTrace());
				e.printStackTrace();
		}
	    return builder.toString();
	}

	/**
  	 * Returns an http url of ApplicationMaster's container logs
  	 * 
  	 * @author Vassilis Papaioannou
  	 * @parm conf				YARN cluster configuration
  	 * @param					running application's name
  	 * @return amContainerLogs	the running containers per node
  	 */
	public static String issueRequestApplicationLogs( YarnConfiguration conf, String applicationId) throws Exception {
		String masterDNS = conf.get( "yarn.resourcemanager.webapp.address");
		String urlString = "http://" + masterDNS + "/ws/v1/cluster/apps/" + applicationId;
		StringBuilder builder = null;
		String xmlStr = null;
		InputStream in = null;
		HashMap< String, String> metrics = new HashMap< String, String>();
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

	
	/**
  	 * Returns a ConcurrentHashMap having as keys the cluster nodes and as values
  	 * the container ids running on them
  	 * 
  	 * @author Vassilis Papaioannou
  	 * @parm conf					YARN cluster configuration
  	 * @param nodes					cluster nodes
  	 * @param						running application's name
  	 * @return runningContainers 	the running containers per node
  	 */
	public static ConcurrentHashMap< String, ArrayList<String>> issueRequestApplicationContainersLogs( YarnConfiguration conf, ConcurrentHashMap< String, String> nodes, String appname) throws Exception {
		String urlString = null;
		URL url = null;
		HttpURLConnection con = null;
		StringBuilder builder = null;
		byte[] buffer = null;
		int count = 0;
		String xmlStr = null;
		InputStream in = null;
		ConcurrentHashMap< String, ArrayList<String>> runningContainers = new ConcurrentHashMap< String, ArrayList< String>>();
		for( Entry<String, String> node : nodes.entrySet()){
			urlString = "http://" + node.getKey() + ":" + node.getValue() + "/ws/v1/node/apps/" + appname;
			try {
				url = new URL(urlString);
				con = (HttpURLConnection) url.openConnection();
			
				con.setRequestMethod("GET");
				con.setRequestProperty("accept", "application/xml");
				con.setRequestProperty("Content-type", "application/xml");	        
				con.setDoInput(true);
				//not all nodes have running containers for this application
				if( con.getResponseCode() == 500){
					continue;
				}			
				builder = new StringBuilder();
				in = con.getInputStream();
				buffer = new byte[1024];
				while((count = in.read(buffer))!=-1) {
					builder.append(new String(buffer,0,count) + "\n");
				}
				String[] tmetrics = null;
				xmlStr = builder.toString();
				xmlStr = xmlStr.replaceAll( "<\\?[^>]+\\?>", "" );
			        xmlStr = xmlStr.replaceAll( "<app>", "");
				xmlStr = xmlStr.replaceAll( "</[^>]+>", "\n");
				xmlStr = xmlStr.replaceAll( "[<]+", "");
				xmlStr = xmlStr.replaceAll( "[>]+", " ").trim();
				tmetrics = xmlStr.split( "\n");
				//System.out.println( "NODE " + node.getKey() +" at port " + node.getValue() +" INFO:\n" + xmlStr);
				
				for( int i = 0; i < tmetrics.length; i++){
					//System.out.println( tmetrics[ i]);
					if( !tmetrics.equals( "") && tmetrics[ i].trim().indexOf( " ") > -1){
						if( tmetrics[ i].startsWith( "containerids")){
							if( runningContainers.get( node.getKey()) == null){
								runningContainers.put( node.getKey(), new ArrayList< String>());
							}
							runningContainers.get( node.getKey()).add( tmetrics[ i].split( " ")[ 1]);
						}
					}
				}			
			} 
			catch (Exception e)
			{
					LOG.error( e.getStackTrace());
					e.printStackTrace();
			}
		}

	    return runningContainers;
	}
}