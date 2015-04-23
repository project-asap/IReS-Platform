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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Subresource class used to feed the profiler with new metrics and retrieve
 * old ones. Other interactions are implemented in here as well, as model
 * validation, etc.
 * @author Giannis Giannakopoulos
 */
@Path("/application/{application-id}/metrics/")
public class ApplicationMetricsREST {

    //returns metrics
    // query param is one of:
    //  actual      -- returns the profiling metrics only
    //  estimated   -- returns the approximated metrics
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics(@PathParam("application-id") String appId, @QueryParam("type") String metricsType) {
        System.out.println(metricsType);
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    // adds a new actual metric, as measured by the deployment tool
    @PUT
    @Path("add/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertMetric(@PathParam("application-id") String appId) {
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    // gets a metric as described by the InputSpacePoint
    // the type query param can take the same values as above
    @POST
    @Path("get/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetric(@PathParam("application-id") String appId, @QueryParam("type") String metricsType) {
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
}
