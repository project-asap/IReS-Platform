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
package com.cloudera.kitten.appmaster;

import java.io.File;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudera.kitten.appmaster.params.lua.LuaApplicationMasterParameters;
import com.cloudera.kitten.appmaster.params.lua.WorkflowParameters;
import com.cloudera.kitten.appmaster.service.ApplicationMasterServiceImpl;
import com.cloudera.kitten.appmaster.service.WorkflowService;
import com.cloudera.kitten.lua.LuaFields;
import com.google.common.collect.ImmutableMap;

import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;

/**
 * A simple ApplicationMaster to use when there isn't any master logic that is required to run.
 */
public class ApplicationMaster extends Configured implements Tool {

  private static final Log logger = LogFactory.getLog( ApplicationMaster.class);
  //to know if the workflow is at replan state or not
  public static boolean isReplanning = false;
  //to store the new workflow coming from replanning
  public static WorkflowDictionary new_replanned_workflow = null;
  //since the replanned workflow will be executed in the context of the application already running
  //i.e. the same ApplicationMaster will be used for the replanned workflow, there is no need to
  //re - register the ApplicationMaster. By the way the ResourceManager will fail if a re - registration
  //is applied during the start() phase of WorkflowService
  public static RegisterApplicationMasterResponse initial_registration = null;

  @Override
  public int run(String[] args) throws Exception {
	  File script = new File(LuaFields.KITTEN_LUA_CONFIG_FILE);
      System.out.println( "ApplicationMaster runs!" );
      logger.info( "ApplicationMaster runs!" );
	  if(script.exists()){
	  	//single operator
		ApplicationMasterParameters params = new LuaApplicationMasterParameters(getConf());
		ApplicationMasterService service = new ApplicationMasterServiceImpl(params, getConf());
		service.startAndWait();
		while (service.hasRunningContainers()) {
		  Thread.sleep(1000);
		}
		service.stopAndWait();
	  }
	  else{
		  //workflow
          System.out.println( "Setting workflow parameters and service!" );
          logger.info( "Setting workflow parameters and service!" );
		  WorkflowParameters params = new WorkflowParameters(getConf());
		  WorkflowService service = new WorkflowService(params, getConf());
          System.out.println( "Workflow parameters and service have been set!" );
          logger.info( "Workflow parameters and service have been set!" );
          System.out.println( "Starting workflow parameters and service ..." );
          logger.info( "Starting workflow parameters and service ..." );
          service.startAndWait();
          while (service.hasRunningContainers()) {
        	  Thread.sleep(1000);
          }
          service.stopAndWait();
          //it is not known beforehand how many times replanning will be needed but
          //however any replanning must be enforced in the end
          while( isReplanning){
        	  //the workflow plan has been changed so new params and service are needed
        	  params = new WorkflowParameters( new_replanned_workflow, params.jobName, getConf());
        	  service = new WorkflowService( params,getConf());
        	  service.startAndWait();
              while (service.hasRunningContainers()) {
                  Thread.sleep(1000);
              }
              service.stopAndWait();
          }
	  }

	  return 0;
  }

  public static void main(String[] args) throws Exception {
    try {
      int rc = ToolRunner.run(new Configuration(), new ApplicationMaster(), args);
      System.exit(rc);
    } catch (Exception e) {
    	e.printStackTrace();
      System.exit(1);
    }
  }
}
