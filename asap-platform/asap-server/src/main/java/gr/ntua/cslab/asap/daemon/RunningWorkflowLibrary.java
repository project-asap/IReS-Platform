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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.jeval.EvaluationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.cli.YarnCLI;
import org.apache.log4j.Logger;

import com.cloudera.kitten.client.YarnClientService;
import com.cloudera.kitten.client.params.lua.LuaYarnClientParameters;
import com.cloudera.kitten.client.service.YarnClientServiceImpl;
import com.cloudera.kitten.util.LocalDataHelper;

public class RunningWorkflowLibrary {
	private static ConcurrentHashMap<String,WorkflowDictionary> runningWorkflows;
	private static ConcurrentHashMap<String,WorkflowDictionary> toRunWorkflows;
	private static ConcurrentHashMap<String,AbstractWorkflow1> runningAbstractWorkflows;
	private static ConcurrentHashMap<String,YarnClientService> runningServices;
	public static ConcurrentHashMap<String,ApplicationReport> workflowsReport;
	
	private static Configuration conf;
	private static Logger logger = Logger.getLogger(RunningWorkflowLibrary.class.getName());

	public static void initialize() throws Exception{
		runningWorkflows = new ConcurrentHashMap<String, WorkflowDictionary>();
		runningServices = new ConcurrentHashMap<String, YarnClientService>();
		workflowsReport =  new ConcurrentHashMap<String, ApplicationReport>();
		runningAbstractWorkflows=  new ConcurrentHashMap<String, AbstractWorkflow1>();
		toRunWorkflows = new ConcurrentHashMap<String,WorkflowDictionary>();
	    conf = new Configuration();
	    for( Entry<Object, Object> p : ServerStaticComponents.properties.entrySet()){
			conf.set(p.getKey().toString(), p.getValue().toString());
	    }
        (new Thread(new YarnServiceHandler())).start();
	}
	
	public static Map<String,YarnClientService> getRunningServices(){
		return runningServices;
	}
	public static void removeRunningService(String key) throws Exception {
		runningServices.remove(key);
		WorkflowDictionary w = RunningWorkflowLibrary.getWorkflow(key);
		
	    //MaterializedWorkflow1 w = MaterializedWorkflowLibrary.get(e.getKey());
	    for(OperatorDictionary op : w.getOperators()){
			if(op.getIsOperator().equals("true") && op.getStatus().equals("running")){
				Operator operator = OperatorLibrary.getOperator(op.getNameNoID());
				operator.reConfigureModel();
			}
	    }
	}
	
	public static WorkflowDictionary getWorkflow(String name) throws Exception{
		if(toRunWorkflows.containsKey(name)){
			WorkflowDictionary wd = toRunWorkflows.get(name);
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
		else{
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
	}

	public static List<String> getWorkflows() {
		return new ArrayList<String>(runningWorkflows.keySet());
	}

	public static String executeWorkflow(MaterializedWorkflow1 materializedWorkflow) throws Exception {
		runningAbstractWorkflows.put(materializedWorkflow.name, materializedWorkflow.getAbstractWorkflow());
		WorkflowDictionary wd = materializedWorkflow.toWorkflowDictionary("\n");
		for(OperatorDictionary op : wd.getOperators()){
			if(op.getStatus().equals("running"))
				op.setStatus("warn");
		}
		
		YarnClientService service = startYarnClientService(wd, materializedWorkflow);
		runningServices.put(materializedWorkflow.name, service);
		runningWorkflows.put(materializedWorkflow.name, wd);
		return materializedWorkflow.name;
	}

	private static YarnClientService startYarnClientService(WorkflowDictionary d, MaterializedWorkflow1 mw) throws Exception {
	    YarnClientService service = null;
		HashMap<String,String> operators = new HashMap<String, String>();
		HashMap<String,String> inputDatasets = new HashMap<String, String>();
		LuaYarnClientParameters params = null;
		String luafilename = null;
		int luafileindex = 0;

		for(OperatorDictionary op : d.getOperators()){
			if(op.getIsOperator().equals("true")){
				/* vpapa: retrieve the .lua file specified for this operator from
					operator's description
				*/
				luafilename = op.getDescription();
				//check that this property is specified and it has some value
				luafileindex = luafilename.indexOf( "Execution.LuaScript");
				if( luafileindex > -1){
					//found the property inside the description file
					luafilename = luafilename.substring( luafileindex, luafilename.indexOf( "\n", luafileindex));
					luafilename = luafilename.split( "=")[ 1].trim();
					if( luafilename.equals( "")){
						luafilename = op.getNameNoID() + ".lua";
						logger.info( "WARN: The property Execution.LuaScript has not any value. Check if this.");
						logger.info( "In any case, it is assumed that operator's .lua file has the same name");
						logger.info( "as the operator.");
					}
				}
				else{
					logger.info( "WARN: The property Execution.LuaScript is missing from operator's description.");
					logger.info( "Check if this is valid. In any case, it is assumed that operator's .lua file");
					logger.info( "has the same name as the operator");
					luafilename = op.getNameNoID() + ".lua";
				}
				//System.out.println( "The .lua file is: " + luafilename);
				operators.put( op.getName(), OperatorLibrary.operatorDirectory + "/" + op.getNameNoID() + "/" + luafilename);
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
	    /* vpapa: catch a NullPointerException if a .lua is missing
		*/
		try{
			params = new LuaYarnClientParameters( mw.name, tmpFilename, operators, inputDatasets, conf, new HashMap<String, Object>(), new HashMap<String, String>());
		}
		catch( NullPointerException npe){
			logger.info( "ERROR: Check that the .lua file " + luafilename + " exists!");
			logger.info( "It is possible that it is the cause of this exception.");
		}
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

	public static void replan(String id) throws Exception {
		HashMap<String,WorkflowNode> materilizedDatasets = new HashMap<String,WorkflowNode>();
		
		WorkflowDictionary wd = runningWorkflows.get(id);
		MaterializedWorkflow1 materializedWorkflow = new MaterializedWorkflow1(id, "/tmp");
		materializedWorkflow.readFromWorkflowDictionary(wd);
		for(OperatorDictionary op : wd.getOperators()){
			if(op.getIsOperator().equals("false") && op.getIsAbstract().equals("false") && op.getStatus().equals("running")){

				logger.info(op.getName()+" : "+op.getAbstractName());

				WorkflowNode n = new WorkflowNode(false, false,op.getAbstractName());
				Dataset temp = new Dataset(op.getName());
				temp.readPropertiesFromString(op.getDescription().replace("<br>", "\n"));
				n.setDataset(temp);
				materilizedDatasets.put(op.getAbstractName(), n);
			}
		}
		logger.info("Datasets: "+materilizedDatasets);
		
		AbstractWorkflow1 aw = runningAbstractWorkflows.get(id);
		
		
		MaterializedWorkflow1 mwNew =aw.replan(materilizedDatasets, 100);
		toRunWorkflows.put(id, mwNew.toWorkflowDictionary("\n"));
	}


}
