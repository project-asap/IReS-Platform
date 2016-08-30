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


package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.rest.beans.*;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.daemon.RunningWorkflowLibrary;
import gr.ntua.cslab.asap.utils.Utils;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceException;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;

@Path("/runningWorkflows/")
public class RunningWorkflows {

	private static Logger logger = Logger.getLogger(RunningWorkflows.class.getName());

	@GET
	@Produces("application/json")
	@Path("{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.getWorkflow(id);
    }

	@GET
	@Path("{id}/trackingURL")
    public String getTrackingURL(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return RunningWorkflowLibrary.getTrackingUrl(id);
    }

	@GET
	@Path("{id}/application/name")
    public String getApplicationName(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
		String appname = RunningWorkflowLibrary.getTrackingUrl(id);
		return appname.substring( appname.indexOf( "application_"));
    }
	
	@GET
	@Path("{id}/application/logs")
    public String getApplicationLogs(@PathParam("id") String id) throws Exception {
		return RunningWorkflowLibrary.getApplicationLogs(id);
    }
	
	@GET
	@Path("{id}/application/containerLogs")
    public String getApplicationContainersLogs(@PathParam("id") String id) throws Exception {
		return RunningWorkflowLibrary.getApplicationContainersLogs(id);
    }

	@GET
	@Path("{id}/state")
    public String getState(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return RunningWorkflowLibrary.getState(id);
    }
	
	@GET
	@Produces("application/XML")
	@Path("XML/{id}/")
    public WorkflowDictionary getDescriptionXML(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.getWorkflow(id);
    }
	
	@GET
	@Produces("application/XML")
	@Path("toRunWorkflow/XML/{id}/")
    public WorkflowDictionary getDescriptionToRunXML(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.getWorkflowToRun(id);
    }	
	
	@GET
	@Produces("application/XML")
	@Path("replan/{id}/")
    public void replan(@PathParam("id") String id) throws Exception {
        RunningWorkflowLibrary.replan(id);
    }
	
	@POST
    @Path("report/{id}/")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void refreshReport(@PathParam("id") String id, @Context HttpServletRequest request, InputStream input) throws IOException, Exception {
		logger.info("Update state: "+id);
		WorkflowDictionary workflow = Utils.unmarshall(input);
		RunningWorkflowLibrary.setWorkFlow(id, workflow);
	}
}
