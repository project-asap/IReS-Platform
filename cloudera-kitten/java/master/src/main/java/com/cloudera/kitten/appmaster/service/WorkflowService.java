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
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
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

public class WorkflowService extends
    AbstractScheduledService implements ApplicationMasterService,
    AMRMClientAsync.CallbackHandler {

  private static final Log LOG = LogFactory.getLog(WorkflowService.class);

  public final WorkflowParameters parameters;
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
	this.containerAllocation = new HashMap<ContainerId, ContainerTracker>();
    this.resourceManager = AMRMClientAsync.createAMRMClientAsync(1000, this);
    this.resourceManager.init(conf);
    this.resourceManager.start();

    RegisterApplicationMasterResponse registration;
    //has the ApplicationMaster been registered already?
    if( !ApplicationMaster.isReplanning){
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
        ApplicationMaster.initial_registration = registration;
    }
    else{
        if( ApplicationMaster.initial_registration != null){
            registration = ApplicationMaster.initial_registration;
        }
        else{
            LOG.info( "There wasn't any valid registration of ApplicationMaster!");
            stop();
            return;
        }
    }

    factory = new ContainerLaunchContextFactory( registration.getMaximumResourceCapability());

    trackers = parameters.createTrackers(this);

    for(ContainerTracker t : trackers.values()){
    	LOG.info( "Tracker to initialize: " + t);
	    t.init(factory);
    }
    LOG.info( "All trackers has been initialized: " + trackers);
    /*for ( Entry<String, ContainerLaunchParameters> e : parameters.getContainerLaunchParameters().entrySet()) {
    	ContainerTracker tracker = new ContainerTracker(e.getValue());
    	LOG.info("Operator: " + e.getKey());
    	trackers.put(e.getKey(),tracker);
    }
    LOG.info("Trackers: " + trackers);

    trackers.get("Move_MySQL_HBase").addNextTracker(trackers.get("HBase_HashJoin"));
    trackers.get("HBase_HashJoin").addNextTracker(trackers.get("Sort2"));

    trackers.get("Move_MySQL_HBase").init(factory);*/

    this.hasRunningContainers = true;
    //if this WorkflowService created due to a replanning, at this point the replanning
    //has been completed and thus the ApplicationMaster should not be at "replanning" state
    //any longer
    if( ApplicationMaster.isReplanning){
        ApplicationMaster.isReplanning = false;
    }
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
	//WorkflowDictionary new_replanned_workflow = null;

	AbstractClient.issueRequest(conf, parameters.jobName, parameters.workflow);
    response = AbstractClient.issueRequestClusterStatus( conf).split( "\n");
    services_n_status = new HashMap<String, String>();
    for( String servic: response ){
        services_n_status.put( servic.split( ":")[ 0].trim(), servic.split( ":")[ 1].trim());
    }

    for( String s : services_n_status.keySet())
        System.out.println( "Service " + s + " has status " + services_n_status.get( s));

    //read workflow operators' state until the running operators are found and if are found
    //check that operators' needed services are up. If these services are not up replan
    //workflow execution
    for( OperatorDictionary opd : parameters.workflow.getOperators()){
        if( opd.getStatus().toLowerCase().equals( "running") && opd.getIsOperator().toLowerCase().equals( "true")){
            service = opd.getEngine();
            //System.out.println( "\n\nService is " + service);

            if( service != null){
                //System.out.println( "Services and their status " + services_n_status.get( service ));
                if( services_n_status.get( service).toString().equals( "true")){
                    LOG.info( "Service " + service + " is up for operator " + opd.getName());
                }
                else{
                    LOG.info( "Service " + service + " is not up for operator " + opd.getName());
                    LOG.info( "Workflow " + parameters.jobName + " should be replanned.");
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
                    	LOG.info( "ApplicationMaster got in at 'replanning' state");
                    	ApplicationMaster.isReplanning = true;
                    	ApplicationMaster.new_replanned_workflow = reBuildPlan( opd);
                    	stop();
                    	
                    	//since one operator failed and replan has been requested there is no reason to search for other
                        //failed operators since the replan returns a "global" plan
                        break;
                    }//end of if( replan)
                }//end of if( services_n_status.get( service).toString().equals( "true")) but else part
            }//end of if( service != null)
        }//end of if( opd.getStatus().toLowerCase().equals( "running") && opd.getIsOperator().toLowerCase().equals( "true"))
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
            if (tracker.isInitilized && tracker.needsContainers()) {
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
  
  public WorkflowDictionary reBuildPlan( OperatorDictionary faiiledengineopd) throws Exception{
	boolean all_inputs_completed = false;
	String inname = "";
	String outname = "";
	List< String> linput = null;
	ListIterator< String> lis = null;
	OperatorDictionary opdic = null;
	HashMap< String, String> inputs = null;
	HashMap< String, OperatorDictionary> final_replanned_workflow = null;
	HashMap< String, OperatorDictionary> assist_workflow_replan = null;
	WorkflowDictionary replanned_workflow = null;
	WorkflowDictionary before_workflow_replanning = null;
	WorkflowDictionary after_workflow_replanning = null;
	WorkflowDictionary new_replanned_workflow = new WorkflowDictionary();

	//this operator has been failed so mark it
	faiiledengineopd.setStatus( "failed");
    replanned_operators.put( faiiledengineopd.getName(), "true");
    LOG.info( "Replanned operators are\n" + replanned_operators + "\n");
	//and also mark its inputs and its outputs

	linput = faiiledengineopd.getInput();
	lis = linput.listIterator();
	while( lis.hasNext()){
		inname = lis.next();
		LOG.info( "Input: " + inname);
	    for( OperatorDictionary opdd : parameters.workflow.getOperators()){
	    	if( opdd.getName().equals( inname)){
	    		opdd.setStatus( "stopped");
	    		break;
	    	}
	    }
	}
	//then re - plan
	LOG.info( "CURRENT TRACKERS BEFORE REPLANNING: " + trackers);
	before_workflow_replanning = parameters.workflow;
    AbstractClient.issueRequestReplan( conf, parameters.jobName);
    after_workflow_replanning = AbstractClient.issueRequestRunningWorkflow( conf, parameters.jobName);
    replanned_workflow = AbstractClient.issueRequestToRunWorkflow( conf, parameters.jobName);
    LOG.info( "WORKFLOW TO REPLAN is\n");
    for( OperatorDictionary opdd : before_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\n");
    }
    LOG.info( "AFTER WORKFLOW REPLAN\n");
    for( OperatorDictionary opdd : after_workflow_replanning.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\n");
    }
    LOG.info( "REPLANNED WORKFLOW\n");
    for( OperatorDictionary opdd : replanned_workflow.getOperators()){
    	LOG.info( "Operator: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\n");
    }

    //since the output field of operators is empty, extract this information in another way
    //for after_workflow_replanning
    inputs = new HashMap< String, String>();
    for( OperatorDictionary opawr : after_workflow_replanning.getOperators()){
    	//build input output information for operators of after_workflow_replanning
    	if( !opawr.getInput().isEmpty()){
    		inname = "";
    		lis = opawr.getInput().listIterator();
    		while( lis.hasNext()){
    			outname = lis.next();
    			inname += outname + "$";
    		}
    		inputs.put( opawr.getName(), inname);
    	}
    }
    for( Entry< String, String> e : inputs.entrySet()){
    	LOG.info( "Inputs of operator " + e.getKey() + "\tare\t" + e.getValue());
    }
    //convert WorkflowDictionary after_workflow_replanning and replanned_workflow to HashMap
    final_replanned_workflow = new HashMap< String, OperatorDictionary>( 32);
    for( OperatorDictionary opawr : after_workflow_replanning.getOperators()){
    	//if a failed operator has been found update its state accordingly
    	if( opawr.getName().equals( faiiledengineopd.getName())){
    		opawr.setStatus( "failed");
    	}
    	//if an operator has been found that has as input a failed operator, then set this
    	//operator at the "stopped" state
    	/*
        if( inputs.get( opawr.getName()) == null){
            LOG.info( "1. OPERATOR NAME:" + opawr.getName());
        }
        */
    	if( inputs.get( opawr.getName()) != null && inputs.get( opawr.getName()).contains( faiiledengineopd.getName())){
    		opawr.setStatus( "stopped");
    	}
    	final_replanned_workflow.put( opawr.getName(), opawr);
    }
    assist_workflow_replan = new HashMap< String, OperatorDictionary>( 32);
    for( OperatorDictionary oprw : replanned_workflow.getOperators()){
    	assist_workflow_replan.put( oprw.getName(), oprw);
    }
    for( String operator : assist_workflow_replan.keySet()){
    	if( assist_workflow_replan.get( operator).getIsOperator().equals( "true")){
        	if( final_replanned_workflow.get( operator) != null){
        		opdic = final_replanned_workflow.get( operator);
        		//if operator is not at "completed" state
        		if( !opdic.getStatus().equals( "completed")){
        			//update the status of the operator with the new one from replanning
        			opdic.setStatus( assist_workflow_replan.get( operator).getStatus());
        			//update old inputs and outputs of this operator
                    //LOG.info( "1.2 OPERATOR NAME:" + opdic.getName() + "\twith status " + opdic.getStatus());
                    if( opdic.getStatus().equals( "running")){
                        //in order to issue a container for an operator, operator status should be "warn"
                        opdic.setStatus( "warn");
                        //update input
                        lis = final_replanned_workflow.get( operator).getInput().listIterator();
                        while( lis.hasNext()){
                            inname = lis.next();
                            final_replanned_workflow.get( inname).setStatus( "warn");
                        }
                        //update output
                        for( String out : inputs.keySet()){
                            if( inputs.get( out).contains( opdic.getName())){
                                //LOG.info( "INPUT: " + out + "\tOPERATOR: " + opdic.getName());
                                final_replanned_workflow.get( out).setStatus( "warn");
                            }
                        }
                        //"upload" the updated operator
                        final_replanned_workflow.put( operator, opdic);
                    }
        		}
        	}
    	}
    }
    LOG.info( "REBUILT AFTER WORKFLOW REPLAN\n");
	
	for( OperatorDictionary opawr : after_workflow_replanning.getOperators()){
        LOG.info( "AFTER WORKFLOW REPLANNING: " + opawr.getName() + "\twith status " + opawr.getStatus());
    }
	for( String s : final_replanned_workflow.keySet()){
        LOG.info( "FINAL REPLANNED WORKFLOW: " + final_replanned_workflow.get( s).getName() + "\twith status " + final_replanned_workflow.get( s).getStatus());
    }
	for( OperatorDictionary opawr : after_workflow_replanning.getOperators()){
        if( final_replanned_workflow.get( opawr.getName()) == null){
            //LOG.info( "2. OPERATOR NAME:" + opawr.getName());
        }
        if( final_replanned_workflow.get( opawr.getName()) != null){
            opdic = final_replanned_workflow.get( opawr.getName());
            //make the final updates and produce the final workflow
            if( opdic.getStatus().equals( "failed")){
                //update input
                lis = final_replanned_workflow.get( opawr.getName()).getInput().listIterator();
                //LOG.info( "FAILED!");
                while( lis.hasNext()){
                    inname = lis.next();
                    //LOG.info( "FAILED INPUT: " + inname + "\twith status " + final_replanned_workflow.get( inname).getStatus());
                    final_replanned_workflow.get( inname).setStatus( "stopped");
                }
                //update output
                for( String out : inputs.keySet()){
                    if( inputs.get( out).contains( opdic.getName())){
                        final_replanned_workflow.get( out).setStatus( "stopped");
                    }
                }
                //"upload" the updated operator
                final_replanned_workflow.put( opawr.getName(), opdic);
            }
            //we want the first dataset of the new workflow to be at "state" completed
            //it is assumed that the operators are accessed in the same order they are executed
            if( opdic.getStatus().equals( "warn") && opdic.getIsOperator().equals( "false") && !all_inputs_completed){
            	 lis = opdic.getInput().listIterator();
            	 while( lis.hasNext()){
            		 inname = lis.next();
            		 if( final_replanned_workflow.get( inname).getStatus().equals( "completed")){
            			 all_inputs_completed = true;
            		 }
            		 else{
            			 all_inputs_completed = false;
            			 break;
            		 }
            	 }
                 if( all_inputs_completed){
                	 opdic.setStatus( "completed");
                 }            	
            }

		    new_replanned_workflow.addOperator( final_replanned_workflow.get( opawr.getName()));
        }
	}
    for( OperatorDictionary opdd : new_replanned_workflow.getOperators()){
     	LOG.info( "NEW REPLANNED WORKFLOW: " + opdd.getName() + "\twith status " + opdd.getStatus() + "\n");
    }
    LOG.info( "CURRENT TRACKERS AFTER REPLANNING: " + trackers);
	
	return new_replanned_workflow;
  }
}