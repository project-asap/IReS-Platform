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

import gr.ntua.cslab.asap.staticLibraries.ClusterStatusLibrary;

import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/clusterStatus/")
public class ClusterStatus {
    
    @GET
    @Path( "services")
    @Produces(MediaType.TEXT_HTML)
    public String listServices() {
    	String ret = "<ul>";
    	for(Entry<String, Boolean> e : ClusterStatusLibrary.status.entrySet()){
			ret+= "<li>"+e.getKey()+" : "+e.getValue()+"</li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }
    
    @GET
    @Path( "static/resources")
    @Produces(MediaType.TEXT_HTML)
    public String listClusterStaticResources() {
    	String ret = "Resource min max <ul>";
    	for(Entry<String, String> e : ClusterStatusLibrary.cluster_static_resources.entrySet()){
			ret+= "<li>" + e.getKey() + " : " + e.getValue().split( "_")[ 0] + "\t" + e.getValue().split( "_")[ 1] + "</li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }

    @GET
    @Path( "available/resources")
    @Produces(MediaType.TEXT_HTML)
    public String listClusterAvailableResources() {
    	String ret = "<ul>";
    	for(Entry<String, String> e : ClusterStatusLibrary.cluster_available_resources.entrySet()){
			ret+= "<li>"+e.getKey() + " : " + e.getValue()+"</li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }
    
    @GET
    @Path( "services/alive/{id}")
    public void setAlive(@PathParam("id") String id) {
    	ClusterStatusLibrary.setStatus(id, true);
    }    

    @GET
    @Path( "services/dead/{id}")
    public void setDead(@PathParam("id") String id) {
    	ClusterStatusLibrary.setStatus(id, false);
    }
}
