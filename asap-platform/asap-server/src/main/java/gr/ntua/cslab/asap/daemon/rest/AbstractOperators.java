package gr.ntua.cslab.asap.daemon.rest;


import gr.ntua.cslab.asap.daemon.ServerStaticComponents;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDescription;
import gr.ntua.cslab.asap.staticLibraries.AbstractOperatorLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.log4j.Logger;

@Path("/abstractOperators/")
public class AbstractOperators {

    public Logger logger = Logger.getLogger(AbstractOperators.class);
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String listOperators() {
    	List<String> l = AbstractOperatorLibrary.getOperators();
    	String ret = "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }

    @GET
    @Path("{id}/")
    @Produces(MediaType.TEXT_HTML)
    public String getApplicationInfo(@PathParam("id") String id) {
    	return AbstractOperatorLibrary.getOperatorDescription(id);
    }
    
    @GET
    @Path("json/{id}/")
	@Produces("application/json")
    public OperatorDescription getOperatorInfoJson(@PathParam("id") String id) {
    	return AbstractOperatorLibrary.getOperatorDescriptionJSON(id);
    }
    
    
    @GET
    @Path("XML/{id}/")
	@Produces("application/XML")
    public OperatorDescription getOperatorInfoXML(@PathParam("id") String id) {
    	return AbstractOperatorLibrary.getOperatorDescriptionJSON(id);
    }
    

    @GET
    @Path("add/")
	@Produces("application/XML")
    public String addOperator(
            @QueryParam("opname") String opname,
            @QueryParam("opString") String opString) throws Exception {
    	
    	AbstractOperatorLibrary.addOperator(opname, opString);
    	return "OK";
    }
    
    @GET
    @Path("delete/")
	@Produces("application/XML")
    public String deleteOperator(
            @QueryParam("opname") String opname) throws Exception {
    	
    	AbstractOperatorLibrary.deleteOperator(opname);
    	return "OK";
    }
    
    @GET
    @Path("checkMatches/")
	@Produces("application/XML")
    public String checkMatches(
            @QueryParam("opname") String opname,
            @QueryParam("opString") String opString) throws Exception {
    	AbstractOperator o = new AbstractOperator(opname);
    	InputStream is = new ByteArrayInputStream(opString.getBytes());
    	o.readPropertiesFromFile(is);
    	List<Operator> ops = OperatorLibrary.getMatchesNoTempID(o);
    	String ret = "";
    	for(Operator op: ops ){
    		ret+=op.opName+"&&";
    	}
    	return ret;
    }
}