package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.rest.beans.*;
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
	@Path("/{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.getWorkflow(id);
    }

	@GET
	@Path("/{id}/trackingURL")
    public String getTrackingURL(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return RunningWorkflowLibrary.getTrackingUrl(id);
    }

	@GET
	@Path("/{id}/state")
    public String getState(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return RunningWorkflowLibrary.getState(id);
    }

	@GET
	@Produces("application/XML")
	@Path("/XML/{id}/")
    public WorkflowDictionary getDescriptionXML(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.getWorkflow(id);
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
