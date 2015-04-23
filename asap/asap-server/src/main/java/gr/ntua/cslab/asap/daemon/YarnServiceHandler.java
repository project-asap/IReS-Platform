package gr.ntua.cslab.asap.daemon;

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
    		for(Entry<String, YarnClientService> e : services.entrySet()){
    			handle(e);
    		}
    		
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	private void handle(Entry<String, YarnClientService> e) {
		YarnClientService service = e.getValue();
		ApplicationReport report = null;
		if (service.isRunning()) {
			report = service.getApplicationReport();
	        
		}
		else{
			report = service.getFinalReport();
	    	service.stop();
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