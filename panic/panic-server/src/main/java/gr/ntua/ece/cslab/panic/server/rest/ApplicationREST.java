/*
 * Copyright 2014 Giannis Giannakopoulos.
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

package gr.ntua.ece.cslab.panic.server.rest;

import gr.ntua.ece.cslab.panic.server.containers.Application;
import gr.ntua.ece.cslab.panic.server.containers.beans.ApplicationInfo;
import gr.ntua.ece.cslab.panic.server.containers.beans.ProfilingInfo;
import gr.ntua.ece.cslab.panic.server.shared.ApplicationList;
import gr.ntua.ece.cslab.panic.server.shared.SystemLogger;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implements the application related API calls.
 * @author Giannis Giannakopoulos
 */

@Path("/application/")
public class ApplicationREST {

    // returns a list of the submitted applications
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String,String> listApplications() {
        SystemLogger.get().info("listing available applications");
        return ApplicationList.getShortList();
    }
    
    // creates a new application
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public HashMap<String,String> newApplication(ApplicationInfo application) {
        Application app = new Application();
        app.setAppInfo(application);
        String id = ApplicationList.add(app);
        ApplicationList.get(id).getAppInfo().setId(id);
        SystemLogger.get().info("new application created with id "+id);
        HashMap<String,String>  map = new HashMap<>();
        map.put("id", id);
        return map;
    }
    
    // return "static" application info
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/")
    public Response getApplication(@PathParam("id") String id) {
        Application app = ApplicationList.get(id);
        if(app == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        else
            return Response.ok(app.getAppInfo()).build();
    }
    
    // used to delete an application
    @DELETE
    @Path("{id}/")
    public Response deleteApplication(@PathParam("id") String id) {
        SystemLogger.get().info("delete application with id "+id);
        if(ApplicationList.get(id)==null)
            return Response.status(Response.Status.NOT_FOUND).build();
        else {
            ApplicationList.remove(id);
            return Response.status(Response.Status.OK).build();
        }
    }

    // provide profiling details 
    @PUT
    @Path("{id}/profiling/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response profilingInfo(ProfilingInfo profilingInfo, @PathParam("id") String id) {
        Application app = ApplicationList.get(id);
        if(app==null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            app.setProfilingDetails(profilingInfo);
            return Response.status(Response.Status.OK).build();
        }
    }
    
    // returns profiling details for the specified application
    @GET
    @Path("{id}/profiling/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilingInfo(@PathParam("id") String id) {
        Application app = ApplicationList.get(id);
        if(app == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(app.getProfilingDetails()).build();
        }
    }

    
}
