package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.rest.beans.*;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.daemon.AbstractWorkflowLibrary;

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
	@Path("/{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return AbstractWorkflowLibrary.getWorkflow(id, "<br>");
    }
	

    @GET
    @Path("/test/{id}/")
    @Produces(MediaType.TEXT_HTML)
    public void materializeAbstractWorkflow(@PathParam("id") String id) throws Exception{
    	

		AbstractWorkflow1 aw = new AbstractWorkflow1("test"+id);
		
    }
}
