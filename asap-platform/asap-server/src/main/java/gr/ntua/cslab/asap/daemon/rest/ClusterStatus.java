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
    @Produces(MediaType.TEXT_HTML)
    public String listOperators() {
    	String ret = "<ul>";
    	for(Entry<String, Boolean> e : ClusterStatusLibrary.status.entrySet()){
			ret+= "<li>"+e.getKey()+" : "+e.getValue()+"</li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }
    

    @GET
    @Path("alive/{id}")
    public void setAlive(@PathParam("id") String id) {
    	ClusterStatusLibrary.setStatus(id, true);
    }
    

    @GET
    @Path("dead/{id}")
    public void setDead(@PathParam("id") String id) {
    	ClusterStatusLibrary.setStatus(id, false);
    }
}
