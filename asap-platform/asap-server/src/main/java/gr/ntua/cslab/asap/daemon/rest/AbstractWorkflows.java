package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.rest.beans.*;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.daemon.AbstractWorkflowLibrary;
import gr.ntua.cslab.asap.daemon.RunningWorkflowLibrary;

import java.io.IOException;
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

@Path("/abstractWorkflows/")
public class AbstractWorkflows {

	@GET
	@Produces("application/json")
	@Path("{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return AbstractWorkflowLibrary.getWorkflow(id, "<br>");
    }

	
	@GET
	@Produces("application/XML")
	@Path("XML/{id}/")
    public WorkflowDictionary getDescriptionXML(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return AbstractWorkflowLibrary.getWorkflow(id, "<br>");
    }
	
	
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("add/{id}/")
    public void addWorkflow(@PathParam("id") String id,
    		WorkflowDictionary workflow) throws IOException, NumberFormatException, EvaluationException {
        
		AbstractWorkflowLibrary.addWorkflow(id, workflow);
		
    }
	
	@GET
	@Produces("application/XML")
	@Path("remove/{id}/")
    public void removeWorkflow(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        AbstractWorkflowLibrary.removeWorkflow(id);
    }
	
	@GET
	@Produces("application/XML")
	@Path("materialize/{id}/")
    public String materializeWorkflow(@PathParam("id") String id,@QueryParam("policy") String policy) throws Exception {
        return AbstractWorkflowLibrary.getMaterializedWorkflow(id, policy);
    }

	@GET
	@Path("execute/{id}")
    public void execute(@PathParam("id") String id) throws Exception {
        RunningWorkflowLibrary.executeWorkflow(MaterializedWorkflowLibrary.get(id));
    }
}
