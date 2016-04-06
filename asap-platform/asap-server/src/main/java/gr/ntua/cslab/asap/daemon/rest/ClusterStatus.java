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
