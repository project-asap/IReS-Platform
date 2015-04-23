package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.rest.beans.*;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;

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

@Path("/workflows/")
public class Workflows {

	@GET
	@Produces("application/json")
    public WorkflowDictionary listOperators() throws IOException, NumberFormatException, EvaluationException {

		MaterializedWorkflow1 mw = new MaterializedWorkflow1("latest","/tmp");
		WorkflowDictionary ret = mw.toWorkflowDictionary("\n");
		
		
    	/*WorkflowDictionary ret = new WorkflowDictionary();
    	Random ran = new Random();
    	OperatorDictionary op = new OperatorDictionary("test", ran.nextInt(1000)+"", "stopped");
    	OperatorDictionary op1 = new OperatorDictionary("test1", ran.nextInt(1000)+"", "running");
    	op1.addInput("test");
    	OperatorDictionary op2 = new OperatorDictionary("test2", ran.nextInt(1000)+"", "stopped");
    	op2.addInput("test");
    	OperatorDictionary op3 = new OperatorDictionary("test3", ran.nextInt(1000)+"", "stopped");
    	op3.addInput("test1");
    	op3.addInput("test2");
    	

    	ret.addOperator(op);
    	ret.addOperator(op1);
    	ret.addOperator(op2);
    	ret.addOperator(op3);*/
    	
        return ret;
    }

	@GET
	@Produces("application/json")
	@Path("/{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return MaterializedWorkflowLibrary.getWorkflow(id, "<br>");
    }
	

	@GET
	@Produces("application/XML")
	@Path("/XML/{id}/")
    public WorkflowDictionary getDescriptionXML(@PathParam("id") String id) throws IOException, NumberFormatException, EvaluationException {
        return MaterializedWorkflowLibrary.getWorkflow(id, "<br>");
    }
}
