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

    private static String header = WebUI.readFile("header.html");
    private static String footer = WebUI.readFile("footer.html");

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String listOperatorsFormated() {
        String action = "start";
        String script = "<script>setTimeout('location.reload(true);', 5000)</script>";
        //String script = "<script> $( '#cluster_services').load(  clusterStatus);</script>";
        String ret = header + script;
        ret += "<table id='cluster_services' border='1' align='center' style='width:60%'><tr><th>Service</th><th>Status</th><th>Action</th></tr>";
    	for(Entry<String, Boolean> e : ClusterStatusLibrary.status.entrySet()){
            if( e.getValue()){
                action ="<form action='/clusterStatus/dead/" + e.getKey() + "' method='get'>"
                            + "<input type='hidden' name='service' value='" + e.getKey() + "'>"
                            + "<p align='center'><input class='styled-button' type='submit' value='stop'></form>";
                //action = "<a href='hdp1.itc.unipi.it:1323/clusterStatus/dead/" + e.getKey() + "' target='_blank'>stop</a>";
            }
            else{
                //action = "<a href='hdp1.itc.unipi.it:1323/clusterStatus/alive/" + e.getKey() + "' target='_blank'>start</a>";
                action ="<form action='/clusterStatus/alive/" + e.getKey() + "' method='get'>"
                            + "<input type='hidden' name='service' value='" + e.getKey() + "'>"
                            + "<p align='center'><input class='styled-button' type='submit' value='start'></form>";
            }
			ret+= "<tr><td>"+e.getKey()+"</td><td>"+e.getValue()+"</td><td>" + action + "</td></tr>";

    	}
    	ret+="</table>" + footer;
        return ret;
    }

    @GET
    @Path("raw/")
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
