/**
 * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.kitten.appmaster.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.cloudera.kitten.ContainerLaunchContextFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.Service;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerExitStatus;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import com.cloudera.kitten.ContainerLaunchParameters;
import com.cloudera.kitten.appmaster.AbstractClient;
import com.cloudera.kitten.appmaster.ApplicationMaster;
import com.cloudera.kitten.appmaster.ApplicationMasterParameters;
import com.cloudera.kitten.appmaster.ApplicationMasterService;
import com.cloudera.kitten.appmaster.params.lua.WorkflowParameters;
import com.cloudera.kitten.lua.LuaFields;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractScheduledService;

import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

public class WorkflowService extends
    AbstractScheduledService implements ApplicationMasterService,
    AMRMClientAsync.CallbackHandler {

  private static final Log LOG = LogFactory.getLog(WorkflowService.class);

  //public final WorkflowParameters parameters;
  public WorkflowParameters parameters;
  public final YarnConfiguration conf;
  private final AtomicInteger totalFailures = new AtomicInteger();
  private HashMap<String,ContainerTracker> trackers;
  private HashMap<ContainerId, ContainerTracker> containerAllocation;
  public int prior;
  public AMRMClientAsync<ContainerRequest> resourceManager;
  private boolean hasRunningContainers = false;
  private Throwable throwable;
  public HashMap< String, String> services_n_status = null;
  public HashMap< String, String> replanned_operators = null;
  public static boolean isReplanning = false;
  
  
protected ContainerLaunchContextFactory factory;

  public WorkflowService(WorkflowParameters parameters, Configuration conf) {
	this.trackers = new HashMap<String, ContainerTracker>();
    this.parameters = Preconditions.checkNotNull(parameters);
    this.conf = new YarnConfiguration(conf);
    this.prior=1;
  }

  @Override
  public ApplicationMasterParameters getParameters() {
    return parameters;
  }

  @Override
  public boolean hasRunningContainers() {
    return hasRunningContainers;
  }

  @Override
  protected void startUp() throws IOException {
    RegisterApplicationMasterResponse registration;

	this.containerAllocation = new HashMap<ContainerId, ContainerTracker>();
    this.resourceManager = AMRMClientAsync.createAMRMClientAsync(1000, this);
    this.resourceManager.init(conf);
    this.resourceManager.start();
    
	try {
        registration = resourceManager.registerApplicationMaster(
                parameters.getHostname(),
                parameters.getClientPort(),
                parameters.getTrackingUrl());
    } catch (Exception e) {
        LOG.error("Exception thrown registering application master", e);
        stop();
        return;
    }

    factory = new ContainerLaunchContextFactory( registration.getMaximumResourceCapability());

    trackers = parameters.createTrackers(this);

    for(ContainerTracker t : trackers.values()){
    	LOG.info( "Tracker to initialize: " + t);
	    t.init(factory);
    }
    LOG.info( "All trackers have been initialized: " + trackers);
    /*for ( Entry<String, ContainerLaunchParameters> e : parameters.getContainerLaunchParameters().entrySet()) {
    	ContainerTracker tracker = new ContainerTracker(e.getValue());
    	LOG.info("Operator: " + e.getKey());
    	trackers.put(e.getKey(),tracker);
    }
    LOG.info("Trackers: " + trackers);

    trackers.get("Move_MySQL_HBase").addNextTracker(trackers.get("HBase_HashJoin"));
    trackers.get("HBase_HashJoin").addNextTracker(trackers.get("Sort2"));

    trackers.get("Move_MySQL_HBase").init(factory);*/
    ApplicationMaster.initial_registration = registration;
    this.hasRunningContainers = true;
  }

  @Override
  protected void shutDown() {
    // Stop the containers in the case that we're finishing because of a timeout.
    LOG.info("Stopping trackers");
    this.hasRunningContainers = false;

    for (ContainerTracker tracker : trackers.values()) {
      if (tracker.hasRunningContainers()) {
        tracker.kill();
      }
    }

    FinalApplicationStatus status;
    String message = null;
    if (state() == State.FAILED || totalFailures.get() > parameters.getAllowedFailures()) {
      //TODO: diagnostics
      status = FinalApplicationStatus.FAILED;
      if (throwable != null) {
        message = throwable.getLocalizedMessage();
      }
    } else {
      status = FinalApplicationStatus.SUCCEEDED;
    }
    LOG.info("Sending finish request with status = " + status);
    try {
      resourceManager.unregisterApplicationMaster(status, message, null);
    } catch (Exception e) {
      LOG.error("Error finishing application master", e);
    }    	
  }


  @Override
  protected Scheduler scheduler() {
    return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
  }

  @Override
  protected void runOneIteration() throws Exception {
  	String service = null;
	String[] response = null;
	boolean replan = false;
	
	AbstractClient.issueRequest(conf, parameters.jobName, parameters.workflow);
    response = AbstractClient.issueRequestClusterStatus( conf).split( "\n");
    services_n_status = new HashMap<String, String>();
    for( String servic: response ){
        services_n_status.put( servic.split( ":")[ 0].trim(), servic.split( ":")[ 1].trim());
    }

    //read workflow operators' state until the running operators are found and if are found
    //check that operators' needed services are up. If these services are not up replan
    //workflow execution
    for( OperatorDictionary opd : parameters.workflow.getOperators()){
        if( opd.getStatus().toLowerCase().equals( "running") && opd.getIsOperator().toLowerCase().equals( "true")){
            service = opd.getPropertyValue( "Constraints.Engine");
            //System.out.println( "\n\nService is " + service);

            if( service != null){
                //System.out.println( "Services and their status " + services_n_status.get( service ));
                if( services_n_status.get( service) == null){
                	LOG.info( "Engine " + service + " does not exist! This engine is the engine of operator");
                	LOG.info( opd.getName() + ". Check the description file of this operator for its engine");
                	LOG.info( "or add this engine to cluster's services.");
                }
                else {
                	if( services_n_status.get( service).toString().equals( "true")){
                		LOG.info( "Service " + service + " is up for operator " + opd.getName());	
                	}
                    else{
                        //LOG.info( "Service " + service + " is down for operator " + opd.getName());
                        //LOG.info( "Workflow " + parameters.jobName + " should be replanned.");
                        //replan for this operator if it hasn't replanned earlier
                        if( replanned_operators == null){
                        	replanned_operators = new HashMap< String, String>();
                        	replan = true;
                        }
                        else{
    	                    if( replanned_operators.isEmpty() || replanned_operators.get( opd.getName()) == null){
    	                    	replan = true;
    	                    }
                        }
                        if( replan){
                        	LOG.info( "ApplicationMaster got in 'replanning' state");
                        	ApplicationMaster.isReplanning = true;
                        	parameters.workflow = reBuildPlan( opd.getName());
                        	enforcePlan();
                        	ApplicationMaster.isReplanning = false;
                      	  	LOG.info( "ApplicationMaster got out from 'replanning' state");
                        	//since one operator failed and replan has been requested there is no reason to search for other
                            //failed operators since the replan returns a "global" plan
                            break;
                        }//end of if( replan)
                    }//end of if( services_n_status.get( service).toString().equals( "true")) but else part
                }//end of if( services_n_status.get( service) == null) but else part
            }//end of if( service != null)
        }//end ofMaterialzedWorkflow1 if( opd.getStatus().toLowerCase().equals( "running") && opd.getIsOperator().toLowerCase().equals( "true"))
    }//end of for( OperatorDictionary opd : parameters.workflow.getOperators()){

    if (totalFailures.get() > parameters.getAllowedFailures() || allTrackersFinished()) {
      stop();
    }
  }

  private boolean allTrackersFinished() {
	  boolean ret = true;
	  for(ContainerTracker t : trackers.values()){
		  if(t.hasMoreContainers()){
			 ret =false;
			 break;
		  }
	  }
	  //LOG.info("allTrackersFinished: "+ret);
	  return ret;
  }

// AMRMClientHandler methods
  @Override
  public void onContainersCompleted(List<ContainerStatus> containerStatuses) {
    LOG.info(containerStatuses.size() + " containers have completed");
    for (ContainerStatus status : containerStatuses) {
      int exitStatus = status.getExitStatus();
      if (0 != exitStatus) {
        // container failed
        if (ContainerExitStatus.ABORTED != exitStatus) {
            totalFailures.incrementAndGet();
      	  containerAllocation.remove(status.getContainerId()).containerCompleted(status.getContainerId());
        } else {
          // container was killed by framework, possibly preempted
          // we should re-try as the container was lost for some reason
        }
      } else {
        // nothing to do
        // container completed successfully
          LOG.info("Container id = " + status.getContainerId() + " completed successfully");
    	  containerAllocation.remove(status.getContainerId()).containerCompleted(status.getContainerId());
      }
    }
  }

  @Override
  public void onContainersAllocated(List<Container> allocatedContainers) {
    LOG.info("Allocating " + allocatedContainers.size() + " container(s).");
    Set<Container> assigned = Sets.newHashSet();
    for (ContainerTracker tracker : trackers.values()) {
        for (Container allocated : allocatedContainers) {
            if (tracker.isInitialized && tracker.needsContainers()) {
	          if (!assigned.contains(allocated) && tracker.matches(allocated)) {
	        	  LOG.info("Allocated vcores: "+allocated.getResource().getVirtualCores());
	              tracker.launchContainer(allocated);
	              assigned.add(allocated);
	              containerAllocation.put(allocated.getId(), tracker);
	          }
            }
        }
    }
    for(Entry<ContainerId, ContainerTracker> e: containerAllocation.entrySet()){
    	LOG.info("Allocated: "+e.getKey()+" to operator: "+e.getValue().params.getName());
    }
    /*if (assigned.size() < allocatedContainers.size()) {
      LOG.error(String.format("Not all containers were allocated (%d out of %d)", assigned.size(),
          allocatedContainers.size()));
      stop();
    }*/
  }

  @Override
  public void onShutdownRequest() {
    stop();
  }

  @Override
  public void onNodesUpdated(List<NodeReport> nodeReports) {
    //TODO
  }

  @Override
  public float getProgress() {
    int num = 0, den = 0;
    for (ContainerTracker tracker : trackers.values()) {
      num += tracker.completed.get();
      den += tracker.params.getNumInstances();
    }
    if (den == 0) {
      return 0.0f;
    }
    return ((float) num) / den;
  }

  @Override
  public void onError(Throwable throwable) {
    this.throwable = throwable;
    stop();
  }
 
  /**
   * Enforces the new workflow execution plan
   * 
   * @author Vassilis Papaioannou
   * @throws Exception
   */
  private void enforcePlan() throws Exception {
	  FinalApplicationStatus status;
	  String message = null;
	  LOG.info( "CURRENT TRACKERS BEFORE ENFORCING: " + trackers);
	  
	  //kill the failed trackers( status 'failed') and empty their nextTrackers field assuming that
	  //the next trackers depend on the failed one
	  for( Entry< String, ContainerTracker> tracker : trackers.entrySet()) {
		  if( parameters.workflow.getOperator( tracker.getKey()) != null && parameters.workflow.getOperator( tracker.getKey()).getStatus().equals( "failed")){
			  LOG.info( "Killing tracker: " + tracker.getKey() + " with container: " + tracker.getValue());
			  tracker.getValue().kill();
		  }
	  }
	  //if the workflow has not been updated, stop execution with failed status
	  if( !parameters.workflow.isUpdated){
		  message = "Due to engine(s) not running, the workflow got in replanning mode. "
				  	+ "No alternative execution plan could be found! "
				  	+ "Thus the workflow cannot be executed and is forced to fail!";
		  LOG.info( message);
		  status = FinalApplicationStatus.FAILED;
		  LOG.info("Sending finish request with status = " + status);
		  try {
		      resourceManager.unregisterApplicationMaster( status, message, null);
		  } catch (Exception e) {
		      LOG.error( "Error finishing application master", e);
		  }
	  }
	  else{
		  int applicationId = AbstractClient.issueRequestReport( conf, parameters.jobName);
		  parameters.resetWorkflowParameters( applicationId);
		  LOG.info( "WORKFLOW PARAMETERS HAVE BEEN UPDATED");
		  LOG.info( "CREATING TRACKERS");
		  //resetting priority
		  prior = 1;
		  factory = new ContainerLaunchContextFactory( ApplicationMaster.initial_registration.getMaximumResourceCapability());

		  trackers = parameters.createTrackers(this);
		  LOG.info( "NEWLY CREATED TRACKERS: " + trackers);
		  /*
		  LOG.info( "PARAMETERS WORKFLOW\n");
		  for( OperatorDictionary opdd : parameters.workflow.getOperators()){
			LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\n");
		  }
		  */
		  for(ContainerTracker t : trackers.values()){
			  LOG.info( "Tracker to initialize: " + t);
			  t.init(factory);
		  }
		  LOG.info( "All newly trackers have been initialized: " + trackers);
		  LOG.info( "REPLANNED WORKFLOW HAS STARTED");

		 //this.hasRunningContainers = true;
	  }
  }

  
  /**
   * Builds an alternative path for workflow execution.
   * 
   * @author Vassilis Papaioannou
   * @param faiiledopname the name of the workflow node that failed
   * @throws Exception
   */
  private WorkflowDictionary reBuildPlan( String faiiledopname) throws Exception{
	//boolean all_inputs_completed = false;
	String inname = "";
	List< String> linput = null;
	Iterator< String> lis = null;
	WorkflowDictionary replanned_workflow = null;
	WorkflowDictionary before_workflow_replanning = null;
	WorkflowDictionary after_workflow_replanning = null;

	//LOG.info( "CURRENT TRACKERS BEFORE REPLANNING: " + trackers);
	before_workflow_replanning = parameters.workflow;
        after_workflow_replanning = AbstractClient.issueRequestRunningWorkflow( conf, parameters.jobName);

        //before updating the workflow being executed update the status of the failed operator
        //and its inputs and outputs as failed too
	after_workflow_replanning.getOperator( faiiledopname).setStatus( "failed");
	//mark its inputs
	linput = after_workflow_replanning.getOperator( faiiledopname).getInput();
	lis = linput.listIterator();
	while( lis.hasNext()){
		inname = lis.next();
		LOG.info( "UPDATING FAILED Input: " + inname);
		after_workflow_replanning.getOperator( inname).setStatus( "failed");
	}
	//and its outputs
	for( String opout : after_workflow_replanning.getOutputs( faiiledopname)){
		//LOG.info( "Output operator: " + opout);
		after_workflow_replanning.getOperator( opout).setStatus( "failed");
	}
	//also update the status of 'warn' operators to 'stopped'
	for( OperatorDictionary opdic : after_workflow_replanning.getOperators()){
		if( opdic.getStatus().equals( "warn")){
			opdic.setStatus( "stopped");
			//mark its inputs
			linput = opdic.getInput();
			lis = linput.listIterator();
			while( lis.hasNext()){
				inname = lis.next();
				LOG.info( "UPDATING STOPPED Input: " + inname);
				after_workflow_replanning.getOperator( inname).setStatus( "stopped");
			}
		}
	}
	parameters.workflow = after_workflow_replanning;
	//update the running workflow
	AbstractClient.issueRequest( conf, parameters.jobName, after_workflow_replanning);
	//request a new workflow to substitute the failed branch of the workflow
    AbstractClient.issueRequestReplan( conf, parameters.jobName);
    //retrieve this new workflow
    replanned_workflow = AbstractClient.issueRequestToRunWorkflow( conf, parameters.jobName);
    
    LOG.info( "WORKFLOW TO REPLAN is\n");
    for( OperatorDictionary opdd : before_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\n");
    }

    /*
    LOG.info( "AFTER WORKFLOW REPLAN\n");
    for( OperatorDictionary opdd : after_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\n");
    }
	*/
    
    LOG.info( "REPLANNED WORKFLOW\n");
    for( OperatorDictionary opdd : replanned_workflow.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\n");
    }

    after_workflow_replanning.initiateUpdate( replanned_workflow);
    
    LOG.info( "AFTER WORKFLOW REPLAN - UPDATED\n");
    for( OperatorDictionary opdd : after_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\n");
    }

    MaterializedWorkflow1 mw = new MaterializedWorkflow1( "test", "/tmp");
    mw.readFromWorkflowDictionary( after_workflow_replanning);
    LOG.info( "MW AFTER WORKFLOW REPLAN - UPDATED\n");
    for( OperatorDictionary opdd : after_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\tand inputs " + opdd.getInput() + "\tWorkflowNode " + mw.nodes.get(opdd.getName()) + "\n");
    }
    
	return after_workflow_replanning;
  } 
 } 
