package gr.ntua.cslab.asap.daemon;

import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.utils.Utils;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.jeval.EvaluationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.log4j.Logger;

import com.cloudera.kitten.client.YarnClientService;
import com.cloudera.kitten.client.params.lua.LuaYarnClientParameters;
import com.cloudera.kitten.client.service.YarnClientServiceImpl;

public class RunningWorkflowLibrary {
	private static ConcurrentHashMap<String,WorkflowDictionary> runningWorkflows;
	private static ConcurrentHashMap<String,YarnClientService> runningServices;
	public static ConcurrentHashMap<String,ApplicationReport> workflowsReport;
	
	private static Configuration conf;
	private static Logger logger = Logger.getLogger(RunningWorkflowLibrary.class.getName());

	public static void initialize() throws Exception{
		runningWorkflows = new ConcurrentHashMap<String, WorkflowDictionary>();
		runningServices = new ConcurrentHashMap<String, YarnClientService>();
		workflowsReport =  new ConcurrentHashMap<String, ApplicationReport>();
	    conf = new Configuration();
        (new Thread(new YarnServiceHandler())).start();
	}
	
	public static Map<String,YarnClientService> getRunningServices(){
		return runningServices;
	}
	
	public static WorkflowDictionary getWorkflow(String name) throws Exception{
		WorkflowDictionary wd = runningWorkflows.get(name);
		wd.replaceDescription("\n","<br>");
		/*Random r = new Random();
		for(OperatorDictionary op: wd.getOperators()){
			if(r.nextBoolean()){
				op.setStatus("running");
			}
			else{
				op.setStatus("stopped");
			}
		}*/
		//return mw.toWorkflowDictionary("<br>");
		return wd;
	}

	public static List<String> getWorkflows() {
		return new ArrayList<String>(runningWorkflows.keySet());
	}

	public static void executeWorkflow(MaterializedWorkflow1 materializedWorkflow) throws Exception {
		WorkflowDictionary wd = materializedWorkflow.toWorkflowDictionary("\n");
		for(OperatorDictionary op : wd.getOperators()){
			if(op.getStatus().equals("running"))
				op.setStatus("warn");
		}
		YarnClientService service = startYarnClientService(wd, materializedWorkflow);
		runningServices.put(materializedWorkflow.name, service);
		runningWorkflows.put(materializedWorkflow.name, wd);
	}

	private static YarnClientService startYarnClientService(WorkflowDictionary d, MaterializedWorkflow1 mw) throws Exception {

	    YarnClientService service = null;
		HashMap<String,String> operators = new HashMap<String, String>();
		HashMap<String,String> inputDatasets = new HashMap<String, String>();
		for(OperatorDictionary op : d.getOperators()){
			if(op.getIsOperator().equals("true")){
				operators.put(op.getName(), OperatorLibrary.operatorDirectory+"/"+op.getNameNoID()+"/"+op.getNameNoID()+".lua");
			}
			else{
				if(op.getInput().isEmpty()){
					Dataset inDataset = new Dataset(op.getName());
					inDataset.readPropertiesFromString(op.getDescription());
					logger.info("Adding dataset: "+op.getName()+" "+inDataset.getParameter("Execution.path"));
					inputDatasets.put(op.getName(), inDataset.getParameter("Execution.path"));
				}
			}
		}
		logger.info("Operators: "+operators);
		logger.info("InputDatasets: "+inputDatasets);
		String tmpFilename = mw.directory+"/" +UUID.randomUUID()+".xml";
		Utils.marshall(d, tmpFilename);
	    LuaYarnClientParameters params = new LuaYarnClientParameters(mw.name, tmpFilename, operators, inputDatasets, conf,
	        new HashMap<String, Object>(), new HashMap<String, String>());
	    service = new YarnClientServiceImpl(params);

	    service.startAndWait();
	    if (!service.isRunning()) {
	    	logger.error("Service failed to startup, exiting...");
	    	throw new Exception("Service failed to startup, exiting...");
	    }
	    return service;
	}

	public static String getState(String id) {
		ApplicationReport report = workflowsReport.get(id);
		if(report==null)
			return "";
		else{
			if(report.getYarnApplicationState().equals(YarnApplicationState.FINISHED)){
				String ret = "FINISHED";
				ret+=" "+report.getFinalApplicationStatus();
				return ret;
			}
			else{
				return report.getYarnApplicationState().toString();
			}
		}
	}

	public static String getTrackingUrl(String id) {
		ApplicationReport report = workflowsReport.get(id);
		if(report==null)
			return "";
		else
			return report.getTrackingUrl();
	}

	public static void setWorkFlow(String id, WorkflowDictionary workflow) {
		runningWorkflows.put(id, workflow);
	}
}
