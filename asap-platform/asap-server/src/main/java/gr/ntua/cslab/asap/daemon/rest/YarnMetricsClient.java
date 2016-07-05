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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
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
		
		try {
			//LOG.info("Retrieving metrics from YARN, issuing urlString: " + urlString);
			//System.out.println("ClusterStatus Issuing urlString: " + urlString);
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
			//metrics = (ConcurrentHashMap< String, String>) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString())));
			//LOG.info( "Retrieved metrics: " + metrics);
		} 
		catch (Exception e)
		{
				LOG.error( e.getStackTrace());
				e.printStackTrace();
		}
		    return builder.toString();
		}
}
