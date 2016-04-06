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
	
	public static HashMap< String, String> issueRequestYarnClusterMetrics( YarnConfiguration conf) throws Exception {
		String masterDNS = conf.get( "yarn.resourcemanager.webapp.address");
		String urlString = "http://" + masterDNS + "/wl/v1/cluster/metrics";
		StringBuilder builder = null;
		StringBuffer xmlStr = null;
		InputStream in = null;
		HashMap< String, String> metrics = null;
		JAXBContext jaxbContext = JAXBContext.newInstance( HashMap.class );
		Unmarshaller u = jaxbContext.createUnmarshaller();
		try {
			LOG.info("Retrieving metrics from YARN, issuing urlString: " + urlString);
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
				builder.append(new String(buffer,0,count));
			}
			xmlStr = new StringBuffer( builder.toString());
			metrics = (HashMap< String, String>) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString())));
			LOG.info( "Retrieved metrics: " + metrics);
		} 
		catch (Exception e)
		{
				LOG.error( e.getStackTrace());
				e.printStackTrace();
		}
		    return metrics;
		}
}
