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

import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.log4j.Logger;

import com.cloudera.kitten.client.YarnClientService;

public class YarnServiceHandler implements Runnable {
	private static Logger logger = Logger.getLogger(YarnServiceHandler.class.getName());

    public void run() {
    	while(true){
    		//logger.info("Updating running services");
    		Map<String, YarnClientService> services = RunningWorkflowLibrary.getRunningServices();
    		try {
	    		for(Entry<String, YarnClientService> e : services.entrySet()){
	    			handle(e);
	    		}
    		
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	private void handle(Entry<String, YarnClientService> e) throws Exception {
		YarnClientService service = e.getValue();
		ApplicationReport report = null;
		if (service.isRunning()) {
			report = service.getApplicationReport();
	        
		}
		else{
			
			report = service.getFinalReport();
	    	service.stop();
	    	RunningWorkflowLibrary.removeRunningService(e.getKey());
		}
		//logger.info("State: "+report.getYarnApplicationState());
		RunningWorkflowLibrary.workflowsReport.put(e.getKey(), report);
		
	    
	    
		/*
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
	    else{
	    	
	    }
	    
	    LOG.info("Checking final app report");
	    ApplicationReport report = service.getFinalReport();
	    if (report == null || report.getFinalApplicationStatus() != FinalApplicationStatus.SUCCEEDED) {
	      return 1;
	    }
	    LOG.info("Kitten client finishing...");*/
	}
    
}
