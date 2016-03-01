package gr.ntua.cslab.asap.daemon;

import gr.ntua.cslab.asap.staticLibraries.ClusterStatusLibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.charset.Charset;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;

import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.Runtime;
import java.lang.Runnable;
import java.lang.Thread;
import java.lang.InterruptedException;

import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

public class ClusterNodes extends Configured implements Runnable {

	/* vpapa: add a functionality to know periodically which  nodes are up and down and also know
		which host services are up and down when the host is up.
	*/
	//initialize logs
	private static Logger logger = Logger.getLogger( ClusterNodes.class.getName());

	//define how often the check will be run
	//by default the period is 3 seconds
	int period = 3000;
	//the process that will be returned after checking each service running status
	Process p = null;
	//a BufferedReader to read check's output
	BufferedReader br = null;
	//retrieve the current yarn cluster configuration that is needed from
	//the YarnClient
	YarnConfiguration yconf = new YarnConfiguration();
	//create a YarnClient to retrieve cluster information
	YarnClient yc = YarnClient.createYarnClient();

	//an ArrayList to keep info from YarnClient
	List ycinfo = null;
	//an iterator for the informative ArrayList
	Iterator<NodeReport> yciter = null;
	//node report
	NodeReport nr = null;
	//node state
	NodeState ns = null;

	//define a HashMap to store the information retrieved
	//on a host level i.e. know which hosts are down
	String host = null;
	String service = null;
	String[] hosts = null;
	String[] services = null;
	HashMap< String, String> yhosts = null;
	//on a service level i.e. the host may be up but some
	//services running on it may not
	HashMap< String, String> hservices = null;
	//hservices command to check if they are running
	HashMap< String, String> scommands = null;
	//hservices command output message i.e. status
	HashMap< String, String> sstatus = null;
	//runservices has the hosts where each service is running
	HashMap< String, String> runservices = null;

	public ClusterNodes(){
		//default values
		this( 3000);
	}

	public ClusterNodes( int period){
		this.period = period;
	}
	
	public void run(){
    		//interact with the ResourceManager of YARN cluster and find which nodes are active
    		//and for these nodes check which services are still running on them
		//retrieve the needed information to find which services are running on which
		//hosts
		runservices = new HashMap< String, String>();
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.per-node").split( ",");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.per-node property.");
			logger.info( " Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( " in folder with relative path asap-server/target/conf.");
		}
		hservices = new HashMap< String, String>();
		for( int i = 0; i < hosts.length; i++){
			System.out.println( hosts[ i].trim());
			//services[ 0] -> host, services[ 1] -> host services
			services = hosts[ i].trim().split( ":");
			hservices.put( services[ 0], services[ 1]);
		}
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.check-availability").split( "[;;]{2}");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.check-availability property.");
			logger.info( " Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( " in folder with relative path asap-server/target/conf.");
		}
		scommands = new HashMap< String, String>();
		for( int i = 0; i < hosts.length; i++){
			System.out.println( hosts[ i].trim());
			//services[ 0] -> service, services[ 1] -> service command
			services = hosts[ i].trim().split( ":");
			scommands.put( services[ 0], services[ 1]);
			//add the service into the runservices with an unknown status for the moment
			runservices.put( services[ 0], "");
		}
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.check-status").split( ",");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.check-status property.");
			logger.info( " Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( " in folder with relative path asap-server/target/conf.");
		}
		sstatus = new HashMap< String, String>();
		for( int i = 0; i < hosts.length; i++){
			System.out.println( hosts[ i].trim());
			//services[ 0] -> service, services[ 1] -> service running status
			services = hosts[ i].trim().split( ":");
			sstatus.put( services[ 0], services[ 1]);
		}
		System.out.println( "Yarn nodes services = " + hservices);
		System.out.println( "Services' commands = " + scommands);
		System.out.println( "Running services' status= " + sstatus);
		//initialize and start YarnClient
		yc.init( yconf);
		yc.start();
		//System.out.println( "The yarn cluster has " + hosts.size() + " hosts.");
		yhosts = new HashMap< String, String>();
		try{
			while( true){
				ycinfo = yc.getNodeReports();
				if( ycinfo != null){
					yciter = ycinfo.listIterator();
					while( yciter.hasNext()){
						nr = yciter.next();
						host = nr.getNodeId().toString().split( ":")[ 0];
						yhosts.put( host, nr.getNodeState().toString());
						if( yhosts.get( host).equals( "RUNNING")){
							//retrieve current host's services and
							//for each service check if it is running
							for( String service : hservices.get( host).split( " ")){
								//run the command to get service availability
								p = Runtime.getRuntime().exec( "ssh " + host + " " + scommands.get( service));
								br = new BufferedReader( new InputStreamReader( p.getInputStream()));
								//compare status returned with the one expected from sstatus HashMap if running
								//running status for the current service has been speicified in yarn-site.xml and
								//the command that has been run at the host returned a valid result
								if( sstatus.get( service) != null && br.readLine() != null){
									if( br.readLine().toLowerCase().equals( sstatus.get( service).toLowerCase())){
										//the service is running on current host and so append this
										//host to the hosts on which this service is running
										runservices.put( service, runservices.get( service) + " " + host);
									}
								}
								else{
									//write in logs that for the current service no running status has been specified
								}
								p.destroy();
							}
						}
					}
				}
				for( String service : runservices.keySet()){
					if( runservices.get( service).equals( "")){
						ClusterStatusLibrary.status.put( service, false);
					}
					else{
						ClusterStatusLibrary.status.put( service, true);
					}
				}
				/*
				System.out.println( "The yarn cluster has " + yhosts.size() + " hosts.");
				for( String service : runservices.keySet()){
					System.out.println( "Service: " + service + " runs on: " + runservices.get( service));
				}
				for( String host : yhosts.keySet()){
					System.out.println( "Host: " + host + " has status: " + yhosts.get( host));
				}
				*/
				//preparation for the next iteration
				for( String service : runservices.keySet()){
					runservices.put( service, "");
				}
				Thread.sleep( period);
			}//end of while( true)
		}
		catch( IOException ioe){
			System.out.println( "IOException occured and caught!");
			ioe.printStackTrace();
		}
		catch( YarnException ye){
			System.out.println( "YarnException occured and caught!");
			ye.printStackTrace();
		}
		catch( NullPointerException npe){
			System.out.println( "NullPointerException occured and caught!");
			npe.printStackTrace();
		}
		catch( InterruptedException ie){
			System.out.println( "InterruptedException occured and caught!");
			ie.printStackTrace();
		}
	}// end of run() method
}
