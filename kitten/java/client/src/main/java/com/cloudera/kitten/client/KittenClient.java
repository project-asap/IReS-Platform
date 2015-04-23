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
package com.cloudera.kitten.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;

import com.cloudera.kitten.client.params.lua.LuaYarnClientParameters;
import com.cloudera.kitten.client.service.YarnClientServiceImpl;
import com.google.common.collect.ImmutableMap;

import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.utils.Utils;

/**
 * A simple client for cases where there does not need to be any client-side logic to run a job.
 */
public class KittenClient extends Configured implements Tool {

  private static final Log LOG = LogFactory.getLog(KittenClient.class);
  
  private Map<String, Object> extraLuaValues;
  private Map<String, String> extraLocalResources;
  
  public KittenClient() {
    this(ImmutableMap.<String, Object>of());
  }
  
  public KittenClient(Map<String, Object> extraLuaValues) {
    this(extraLuaValues, ImmutableMap.<String, String>of());
  }
  
  public KittenClient(Map<String, Object> extraLuaValues, Map<String, String> extraLocalResources) {
    this.extraLuaValues = extraLuaValues;
    this.extraLocalResources = extraLocalResources;
  }
  
  @Override
  public int run(String[] args) throws Exception {
    if (!(args.length == 1 || args.length == 2)) {
      System.err.println("Usage: kitten [conf params] <kitten config file>");
      return -1;
    }
    
    Configuration conf = getConf();
    YarnClientService service = null;
    if(args.length == 1){
		HashMap<String,String> operators = new HashMap<String, String>();
		HashMap<String,String> inputDatasets = new HashMap<String, String>();
		WorkflowDictionary d = Utils.unmarshall(args[0]);
		for(OperatorDictionary op : d.getOperators()){
			if(op.getIsOperator().equals("true")){
				operators.put(op.getName(), op.getName()+".lua");
			}
			else{
				if(op.getInput().isEmpty()){
					Dataset inDataset = new Dataset(op.getName());
					inDataset.readPropertiesFromString(op.getDescription());
					System.out.println("Adding dataset: "+op.getName()+" "+inDataset.getParameter("Execution.path"));
					inputDatasets.put(op.getName(), inDataset.getParameter("Execution.path"));
				}
			}
		}
		System.out.println("Operators: "+operators);
		System.out.println("InputDatasets: "+inputDatasets);
	    LuaYarnClientParameters params = new LuaYarnClientParameters(args[0], args[0], operators, inputDatasets, conf,
	        extraLuaValues, extraLocalResources);
	    service = new YarnClientServiceImpl(params);
    }
    else{
    	//single operator
	    LuaYarnClientParameters params = new LuaYarnClientParameters(args[0], args[1], conf,
	        extraLuaValues, extraLocalResources);
	    service = new YarnClientServiceImpl(params);
    }
    return handle(service);
  }

  public int handle(YarnClientService service) throws Exception {
    service.startAndWait();
    if (!service.isRunning()) {
      LOG.error("Service failed to startup, exiting...");
      return 1;
    }
    
    String trackingUrl = null;
    while (service.isRunning()) {
      if (trackingUrl == null) {
        Thread.sleep(1000);
        ApplicationReport report = service.getApplicationReport();
        YarnApplicationState yarnAppState = report.getYarnApplicationState();
        if (yarnAppState == YarnApplicationState.RUNNING) {
          trackingUrl = report.getTrackingUrl();
          if (trackingUrl == null || trackingUrl.isEmpty()) {
            LOG.info("Application is running, but did not specify a tracking URL");
            trackingUrl = "";
          } else {
            LOG.info("Master Tracking URL = " + trackingUrl);
          }
        }
      }
    }
    
    LOG.info("Checking final app report");
    ApplicationReport report = service.getFinalReport();
    if (report == null || report.getFinalApplicationStatus() != FinalApplicationStatus.SUCCEEDED) {
      return 1;
    }
    LOG.info("Kitten client finishing...");
    return 0;
  }
  
  public static void main(String[] args) throws Exception {
    int rc = ToolRunner.run(new Configuration(), new KittenClient(), args);
    System.exit(rc);
  }
}
