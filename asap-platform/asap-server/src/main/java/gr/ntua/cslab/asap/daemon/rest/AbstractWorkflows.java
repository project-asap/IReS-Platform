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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceException;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;

import com.palominolabs.jersey.cors.Cors;
import com.palominolabs.jersey.cors.CorsPreflight;

@Path("/abstractWorkflows/")
public class AbstractWorkflows {

	@GET
    @Cors(allowOrigin="*")  // Change it to specific hosts
	@Produces("application/json")
	@Path("{id}/")
    public WorkflowDictionary getDescription(@PathParam("id") String id) throws Exception {
        return AbstractWorkflowLibrary.getWorkflow(id, "<br>");
    }


	@GET
	@Produces("application/XML")
	@Path("XML/{id}/")
    public WorkflowDictionary getDescriptionXML(@PathParam("id") String id) throws Exception {
        return AbstractWorkflowLibrary.getWorkflow(id, "<br>");
    }


	@POST
    @Cors(allowOrigin="*")  // Change it to specific hosts
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("add/{id}/")
    public void addWorkflow(@PathParam("id") String id,
    		WorkflowDictionary workflow) throws Exception{
		AbstractWorkflowLibrary.addWorkflow(id, workflow);
    }

	@OPTIONS
    @CorsPreflight(allowHeaders="content-type", allowMethods="POST")
	@Path("add/{id}/")
    public void options() throws IOException, NumberFormatException, EvaluationException {
        return;
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
        return AbstractWorkflowLibrary.getMaterializedWorkflow(id, policy,"");
    }

	@GET
	@Produces("application/XML")
	@Path("materializeWithParams/{id}/")
    public String materializeWorkflow(@PathParam("id") String id,@QueryParam("policy") String policy,@QueryParam("parameters") String parameters) throws Exception {
        return AbstractWorkflowLibrary.getMaterializedWorkflow(id, policy,parameters);
    }

	@GET
	@Path("execute/{id}")
    public String execute(@PathParam("id") String id) throws Exception {
        return RunningWorkflowLibrary.executeWorkflow(MaterializedWorkflowLibrary.get(id));
    }
}
