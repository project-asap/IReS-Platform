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
package com.cloudera.kitten.appmaster.params.lua;

import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.utils.Utils;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import com.cloudera.kitten.ContainerLaunchParameters;
import com.cloudera.kitten.appmaster.ApplicationMasterParameters;
import com.cloudera.kitten.appmaster.service.WorkflowService;
import com.cloudera.kitten.appmaster.service.ContainerTracker;
import com.cloudera.kitten.lua.AsapLuaContainerLaunchParameters;
import com.cloudera.kitten.lua.LuaContainerLaunchParameters;
import com.cloudera.kitten.lua.LuaFields;
import com.cloudera.kitten.lua.LuaPair;
import com.cloudera.kitten.lua.LuaWrapper;
import com.cloudera.kitten.util.LocalDataHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.hadoop.net.NetUtils;

public class WorkflowParameters implements ApplicationMasterParameters {

	private static final Log LOG = LogFactory.getLog(WorkflowParameters.class);
	private final HashMap<String,LuaWrapper> env;
	private LuaWrapper e0;
	private final Configuration conf;
	private final Map<String, URI> localToUris;
	private final String hostname;

	private int clientPort = 0;
	private String trackingUrl = "";
	public WorkflowDictionary workflow;
	public MaterializedWorkflow1 materializedWorkflow;
	public String jobName;

  public WorkflowParameters(Configuration conf) throws Exception{
    this(LuaFields.KITTEN_WORKFLOW_CONFIG_FILE, System.getenv(LuaFields.KITTEN_JOB_NAME), conf);
  }

  public WorkflowParameters(Configuration conf, Map<String, Object> extras) throws Exception{
    this(LuaFields.KITTEN_WORKFLOW_CONFIG_FILE, System.getenv(LuaFields.KITTEN_JOB_NAME), conf, extras);
  }

  public WorkflowParameters(String script, String jobName, Configuration conf) throws Exception{
    this(script, jobName, conf, ImmutableMap.<String, Object>of());
  }
  
  public WorkflowParameters(String script, String jobName,
      Configuration conf, Map<String, Object> extras) throws Exception {
    this(script, jobName, conf, extras, loadLocalToUris());
  }
  
  public WorkflowParameters(String script, String jobName,
      Configuration conf,
      Map<String, Object> extras,
      Map<String, URI> localToUris) throws Exception {
	  this.env = new HashMap<String,LuaWrapper>();
		HashMap<String,String> operators = new HashMap<String, String>();

		workflow = Utils.unmarshall(script);
		
		materializedWorkflow = new MaterializedWorkflow1("test", "/tmp");
		materializedWorkflow.readFromWorkflowDictionary(workflow);
		LOG.info(materializedWorkflow.getTargets().get(0).toStringRecursive());
		
		for(OperatorDictionary op : workflow.getOperators()){
			if(op.getIsOperator().equals("true") && op.getStatus().equals("warn")){
				operators.put(op.getName(), op.getName()+".lua");
			}
		}
		for(OperatorDictionary op : workflow.getOperators()){
			if(op.getStatus().equals("warn") && op.getInput().isEmpty()){
				op.setStatus("running");
				workflow.setOutputsRunning(op.getName());
			}
		}
		LOG.info("Operators: "+operators);
		
		int i =0;
		for(Entry<String, String> e : operators.entrySet()){
			LuaWrapper l = new LuaWrapper(e.getValue(), loadExtras(extras)).getTable(e.getKey());
			if(i==0)
				this.e0=l;
			i++;
			this.env.put(e.getKey(),l);
		}
		this.conf = conf;
		this.localToUris = localToUris;
		this.hostname = NetUtils.getHostname();
		this.jobName = jobName;
  }
  
  private static Map<String, URI> loadLocalToUris() {
    Map<String, String> e = System.getenv();
    if (e.containsKey(LuaFields.KITTEN_LOCAL_FILE_TO_URI)) {
      return LocalDataHelper.deserialize(e.get(LuaFields.KITTEN_LOCAL_FILE_TO_URI));
    }
    return ImmutableMap.of();
  }
  
  private static Map<String, Object> loadExtras(Map<String, Object> masterExtras) {
    Map<String, String> e = System.getenv();
    if (e.containsKey(LuaFields.KITTEN_EXTRA_LUA_VALUES)) {
      Map<String, Object> extras = Maps.newHashMap(LocalDataHelper.deserialize(
          e.get(LuaFields.KITTEN_EXTRA_LUA_VALUES)));
      extras.putAll(masterExtras);
      return extras;
    }
    return masterExtras;
  }
  
  @Override
  public Configuration getConfiguration() {
    return conf;
  }
  
  @Override
  public String getHostname() {
    return hostname;
  }

  @Override
  public void setClientPort(int clientPort) {
    this.clientPort = clientPort;
  }

  @Override
  public int getClientPort() {
    return clientPort;
  }

  @Override
  public void setTrackingUrl(String trackingUrl) {
    this.trackingUrl = trackingUrl;
  }

  @Override
  public String getTrackingUrl() {
    return trackingUrl;
  }

  @Override
  public int getAllowedFailures() {
    if (e0.isNil(LuaFields.TOLERATED_FAILURES)) {
      return 4; // TODO: kind of arbitrary, no? :)
    } else {
      return e0.getInteger(LuaFields.TOLERATED_FAILURES);
    }
  }
  
  @Override
  public HashMap<String,ContainerLaunchParameters> getContainerLaunchParameters() {
	  HashMap<String,ContainerLaunchParameters> clp = new HashMap<String, ContainerLaunchParameters>();
	  int i =0;
	  for(Entry<String,LuaWrapper> e : this.env.entrySet()){
		    if (!e.getValue().isNil(LuaFields.CONTAINERS)) {
		      Iterator<LuaPair> iter = e.getValue().getTable(LuaFields.CONTAINERS).arrayIterator();
		      while (iter.hasNext()) {
		    	  String name = "operator_"+i+"_"+e.getKey();
		    	  clp.put(e.getKey(),new LuaContainerLaunchParameters(iter.next().value, name, conf, localToUris));
		    	  i++;
		      }
		      
		    } else if (!e.getValue().isNil(LuaFields.CONTAINER)) {
		    	  //String name = "operator_"+i+"_"+e.getKey();
		    	  String name = e.getKey();
		        try {
					clp.put(e.getKey(),new AsapLuaContainerLaunchParameters(e.getValue().getTable(LuaFields.CONTAINER), name, conf, localToUris, materializedWorkflow, e.getKey()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        i++;
		    }
	  }
	  
      return clp;
  }

	public HashMap<String, ContainerTracker> createTrackers(WorkflowService workflowService) {
		HashMap<String, ContainerTracker> trackers = new HashMap<String, ContainerTracker>();
	    for ( Entry<String, ContainerLaunchParameters> e : getContainerLaunchParameters().entrySet()) {
	    	ContainerTracker tracker = new ContainerTracker(workflowService, e.getValue());
	    	trackers.put(e.getKey(),tracker);
	    }
	    LOG.info("Trackers: " + trackers);
	    
	    for(Entry<String, ContainerTracker> e : trackers.entrySet()){
	    	for(String in : workflow.getOperator(e.getKey()).getInput()){
	    		addTrackerDependencyRecursive(in, e.getKey(), trackers);
	    	}
	    }
	    
		return trackers;
	}
	
	private void addTrackerDependencyRecursive(String in, String out, HashMap<String, ContainerTracker> trackers){
		ContainerTracker outTracker = trackers.get(out);
		ContainerTracker inTracker = trackers.get(in);
		OperatorDictionary inOp = workflow.getOperator(in);
		if(inOp.getStatus().equals("stopped"))
			return;
		if(inOp.getIsOperator().equals("true")){
			LOG.info("Adding previous tracker: " +in+" -> "+out);
			outTracker.addPreviousTracker(inTracker);
			inTracker.addNextTracker(outTracker);
		}
		else{
			//dataset
	    	for(String in1 : inOp.getInput()){
	    		addTrackerDependencyRecursive(in1, out, trackers);
	    	}
		}
	}
}
