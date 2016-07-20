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


package gr.ntua.cslab.asap.daemon;

import gr.ntua.cslab.asap.daemon.rest.YarnMetricsClient;
import gr.ntua.cslab.asap.staticLibraries.ClusterStatusLibrary;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map.Entry;

import java.lang.Process;
import java.lang.Runtime;
import java.lang.Runnable;
import java.lang.Thread;
import java.lang.InterruptedException;

import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
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
	List< NodeReport> ycinfo = null;
	//an iterator for the informative ArrayList
	Iterator< NodeReport> yciter = null;
	//node report
	NodeReport nr = null;
	//node state
	NodeState ns = null;

	//define a HashMap to store the information retrieved
	//on a host level i.e. know which hosts are down
	String host = null;
	String service = null;
	String status = null;
	//in case cluster master node is not a slave and thus it
	//has not have a NodeManager and the relative state report
	String master = null;
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
		master = yconf.get( "yarn.resourcemanager.hostname").trim();
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.per-node").trim().split( ";;");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.per-node property.");
			logger.info( "Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( "in folder with relative path asap-server/target/conf.");
			logger.info( "For this, reliable, real time monitoring of cluster services cannot be established. Until you fix this,");
			logger.info( "the server will run but the services monitoring will not. Have a nice day!");
			logger.info( npe.getMessage());
			//return;
		}
		hservices = new HashMap< String, String>();
		if( hosts != null){
			if( hosts.length > 0){
				for( int i = 0; i < hosts.length; i++){
					//System.out.println( hosts[ i].trim());
					//services[ 0] -> host, services[ 1] -> host services
					services = hosts[ i].trim().split( "::");
					try{
						hservices.put( services[ 0], services[ 1]);	
					}
					catch( ArrayIndexOutOfBoundsException aibe)
					{
						logger.info( "ERROR: for cluster node " + services[ 0] + " no services have been specified.");
						logger.info( "To fix this, in yarn-site.xml file add into the value of property" );
						logger.info( "yarn.nodemanager.services-running.per-node and for node " + services[ 0]);
						logger.info( "add its services. File yarn-site.xml has detailed instructions on how to do it");
						/*String msg = "ERROR: probably the value of yarn.nodemanager.services-running.per-node property.\n";
						msg += "is empty i.e. there is no pair of cluster node and its services. To fix this, add\n";
						msg += "to this property value one cluster node together with its services as it is described\n";
						msg += "into the yarn-site.xml file.";
						logger.log( Level.INFO, "", aibe);
						*/
						//return;
					}
				}
			}
			else{
				logger.info( "WARNING: the value of yarn.nodemanager.services-running.per-node property.");
				logger.info( "is empty. Consequently, no cluster node will be monitored for the status of their services");
			}
		}
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.check-availability").trim().split( ";;");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.check-availability property.");
			logger.info( "Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( "in folder with relative path asap-server/target/conf.");
			logger.info( "For this, reliable, real time monitoring of cluster services cannot be established. Until you fix this,");
			logger.info( "the server will run but the services monitoring will not. Have a nice day!");
			logger.info( npe.getMessage());
			//return;
		}
		scommands = new HashMap< String, String>();
		if( hosts != null){
			if( hosts.length > 0){
				for( int i = 0; i < hosts.length; i++){
					//System.out.println( hosts[ i].trim());
					//services[ 0] -> service, services[ 1] -> service command
					services = hosts[ i].trim().split( "::");
					try{
						scommands.put( services[ 0], services[ 1]);
					}
					catch( ArrayIndexOutOfBoundsException aibe)
					{
						logger.info( "ERROR: for service " + services[ 0] + " there is no command specified with which its status will be");
						logger.info( "checked i.e. if it is running or not. To fix this, in yarn-site.xml file add into the value of property");
						logger.info( "yarn.nodemanager.services-running.check-availability the corresponding command for service");
						logger.info( services[ 0] + ". File yarn-site.xml has detailed instructions on how to do it");
						
						//in any case add this service with an empty command
						scommands.put( services[ 0], "");
					}
					//add the service into the runservices with an unknown status for the moment
					runservices.put( services[ 0], "");
				}
			}
			else{
				logger.info( "WARNING: the value of yarn.nodemanager.services-running.check-availability property.");
				logger.info( "is empty. Consequently, no service will be monitored for its availability");
			}
		}
		try{
			hosts = yconf.get( "yarn.nodemanager.services-running.check-status").trim().split( ";;");
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: YarnConfiguration object cannot find yarn.nodemanager.services-running.check-status property.");
			logger.info( "Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
			logger.info( "in folder with relative path asap-server/target/conf.");
			logger.info( "For this, reliable, real time monitoring of cluster services cannot be established. Until you fix this,");
			logger.info( "the server will run but the services monitoring will not. Have a nice day!");
			logger.info( npe.getMessage());
			//return;
		}
		sstatus = new HashMap< String, String>();
		if( hosts != null){
			if( hosts.length > 0){
				for( int i = 0; i < hosts.length; i++){
					//System.out.println( hosts[ i].trim());
					//services[ 0] -> service, services[ 1] -> service running status
					services = hosts[ i].trim().split( "::");
					try{
						sstatus.put( services[ 0], services[ 1]);	
					}
					catch( ArrayIndexOutOfBoundsException aibe)
					{
						logger.info( "ERROR: for service " + services[ 0] + " there is no status specified with which its state will be.");
						logger.info( "verified i.e. if its running or not. To fix this, in yarn-site.xml file add into the value of property" );
						logger.info( "yarn.nodemanager.services-running.check-status the corresponding status of service " + services[ 0] + ".");
						logger.info( "File yarn-site.xml has detailed instructions on how to do it.");
						/*String msg = "ERROR: probably the value of yarn.nodemanager.services-running.per-node property.\n";
						msg += "is empty i.e. there is no pair of cluster node and its services. To fix this, add\n";
						msg += "to this property value one cluster node together with its services as it is described\n";
						msg += "into the yarn-site.xml file.";
						logger.log( Level.INFO, "", aibe);
						*/
						//return;
					}
				}
			}
			else{
				logger.info( "WARNING: the value of yarn.nodemanager.services-running.check-status property.");
				logger.info( "is empty. Consequently, no service will be monitored for its status, it they are running or not");
			}
		}
		/*	
		System.out.println( "Yarn nodes services = " + hservices);
		System.out.println( "Services' commands = " + scommands);
		System.out.println( "Running services' status= " + sstatus);
		*/
		//initialize and start YarnClient
		//System.out.println( "Yarn Configuration is: " + yconf);
		yc.init( yconf);
		logger.info( "YarnClient has been initiated.");
		yc.start();
		logger.info( "YarnClient has started.");
		//System.out.println( "The yarn cluster has " + hosts.size() + " hosts.");
		yhosts = new HashMap< String, String>();
		try{
			//ensure that the cluster master node will be checked whether or not
			//is also a slave node
			if( yhosts.get( master) == null){
				yhosts.put( master, "RUNNING");
			}
			while( true){
				ycinfo = yc.getNodeReports();
				if( ycinfo != null){
					yciter = ycinfo.listIterator();
					while( yciter.hasNext()){
						nr = yciter.next();
						host = nr.getNodeId().toString().split( ":")[ 0];
						yhosts.put( host, nr.getNodeState().toString());
						//System.out.println( "YARN hosts: " + yhosts);
						//only hosts where NodeManager is running are checked for the availability
						//of their services
					}
					for( String host : yhosts.keySet()){
						if( yhosts.get( host).equals( "RUNNING")){
							//retrieve current host's services and
							//for each service check if it is running
							//logger.info( "HOST\t" + host + "\tHOST SERVICES: " + hservices.get( host));
							if( hservices.get( host) != null){
								for( String service : hservices.get( host).split( " ")){
									//run the command to get service availability
									p = Runtime.getRuntime().exec( "ssh " + host + " " + scommands.get( service));
									br = new BufferedReader( new InputStreamReader( p.getInputStream()));
									//read status
									status = br.readLine();
									/*
									System.out.println( "Service to check: " + service);
									System.out.println( "on host: " + host);
									System.out.println( "with command: " + scommands.get( service));
									System.out.println( "and expected status: " + sstatus.get( service));
									System.out.println( "Actual status: " + status);
									*/
									//compare status returned with the one expected from sstatus HashMap if
									//running status for the current service has been speicified in yarn-site.xml and
									//the command that has been run at the host returned a valid result
									if( sstatus.get( service) != null && status != null){
										if( status.toLowerCase().equals( sstatus.get( service).toLowerCase())){
											//the service is running on current host and so append this
											//host to the hosts on which this service is running
											runservices.put( service, runservices.get( service) + " " + host);
										}
									}
									else{
										//write in logs that for the current service no running
										//status has been specified
									}
									p.destroy();
								}
							}
						}
					}
				}
				//System.out.println( "Running services: " + runservices);
				for( String service : runservices.keySet()){
					//there are some services like Spark that apart from the Master, they
					//need also a Worker running in order to say that this service is actually
					//running
					if( service.toLowerCase().equals( "Spark".toLowerCase())){
						//try to find at least one Spark Worker running
						if( runservices.get( "SparkWorker") != null ){
							if( !runservices.get( "SparkWorker").equals( "")){
								//one Spark Worker has been found and so this service is running
								ClusterStatusLibrary.status.put( service, true);
							}	
						}
					}
					if( service.toLowerCase().equals( "SparkWorker".toLowerCase())){
						//ignore this service because it will be handled during the process
						//of Spark service
						continue;
					}
					//for any other service just check that there is at least one node
					//hosting it
					if( runservices.get( service).equals( "")){
						ClusterStatusLibrary.status.put( service, false);
					}
					else{
						ClusterStatusLibrary.status.put( service, true);
					}
				}
				/*
				System.out.println( "ClusterStatusLibrary: " + ClusterStatusLibrary.status);
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
				//retrieve the minimum and maximum amount of vcores and memory from yarn-site.xml that is a static information
				ClusterStatusLibrary.cluster_static_resources.clear();
				String memory = "";
				String vcores = "";
				try{
					memory = yconf.get( "yarn.scheduler.minimum-allocation-mb") + "_" + yconf.get( "yarn.scheduler.maximum-allocation-mb");
					ClusterStatusLibrary.cluster_static_resources.put( "Memory", memory);
					//System.out.println( "Memory = " + memory);

				}
				catch( NullPointerException npe){
					logger.info( "ERROR: YarnConfiguration object cannot find yarn.scheduler.minimum-allocation-mb or yarn.scheduler.maximum-allocation-mb property.");
					logger.info( " Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
					logger.info( " in folder with relative path asap-server/target/conf.");
				}
				try{
					vcores = yconf.get( "yarn.scheduler.minimum-allocation-vcores") + "_" + yconf.get( "yarn.scheduler.maximum-allocation-vcores");
					ClusterStatusLibrary.cluster_static_resources.put( "VCores", vcores);
					//System.out.println( "Vcores = " + vcores);
				}
				catch( NullPointerException npe){
					logger.info( "ERROR: YarnConfiguration object cannot find yarn.scheduler.minimum-allocation-vcores or yarn.scheduler.maximum-allocation-vcores property.");
					logger.info( " Make sure that this property exists in yarn-site.xml file or that the yarn-site.xml itself exists");
					logger.info( " in folder with relative path asap-server/target/conf.");
				}
				//retrieve the minimum and maximum amount of vcores and memory from YARN rest api that is a dynamic information
				ClusterStatusLibrary.cluster_available_resources.clear();
				//ConcurrentHashMap< String, String> metrics = null;
				String metricsXML = null;
				String[] metrics = null;
				try{
					metricsXML = YarnMetricsClient.issueRequestYarnClusterMetrics( yconf);
					metricsXML = metricsXML.replaceAll( "<\\?[^>]+\\?>", "" );
                    metricsXML = metricsXML.replaceAll( "<clusterMetrics>", "");
					metricsXML = metricsXML.replaceAll( "</[^>]+>", "\n");
					metricsXML = metricsXML.replaceAll( "[<]+", "");
					metricsXML = metricsXML.replaceAll( "[>]+", " ");
					metrics = metricsXML.split( "\n");
				}
				catch( Exception e){
					logger.info( "Something went wrong while metrics from YARN have been asked.");
				}
				for( int i = 0; i < metrics.length; i ++){
					ClusterStatusLibrary.cluster_available_resources.put( metrics[ i].split( " " )[ 0].trim(), metrics[ i].split( " ")[ 1].trim());
					//logger.info( "Metric: " + metrics[ i].split( " ")[ 0 ].trim() + "\t" + metrics[ i].split( " " )[ 1 ].trim());
				}

				Thread.sleep( period);
			}//end of while( true)
		}
		catch( IOException ioe){
			logger.warning( "IOException occured and caught!");
			ioe.printStackTrace();
		}
		catch( YarnException ye){
			logger.warning( "YarnException occured and caught!");
			ye.printStackTrace();
		}
		catch( NullPointerException npe){
			logger.warning( "NullPointerException occured and caught!");
			npe.printStackTrace();
		}
		catch( InterruptedException ie){
			logger.warning( "InterruptedException occured and caught!");
			ie.printStackTrace();
		}
	}// end of run() method
}
