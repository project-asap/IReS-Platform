package gr.ntua.cslab.asap.daemon.rest;

import gr.ntua.cslab.asap.daemon.AbstractWorkflowLibrary;
import gr.ntua.cslab.asap.daemon.Main;
import gr.ntua.cslab.asap.daemon.RunningWorkflowLibrary;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.staticLibraries.AbstractOperatorLibrary;
import gr.ntua.cslab.asap.staticLibraries.DatasetLibrary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import net.sourceforge.jeval.EvaluationException;

import org.apache.log4j.Logger;


@Path("/web/")
public class WebUI {

    public Logger logger = Logger.getLogger(WebUI.class);
    private static String header=readFile("header.html");
    private static String footer=readFile("footer.html");
    private static String runningWorkflowUp=readFile("runningWorkflowUp.html").trim();
    private static String runningWorkflowLow=readFile("runningWorkflowLow.html").trim();
    private static String workflowUp=readFile("workflowUp.html").trim();
    private static String abstractWorkflowUp=readFile("abstractWorkflowUp.html").trim();
    private static String workflowLow=readFile("workflowLow.html");
    private static String scatterPlot=readFile("scatterPlot.html");
    private static String opTreeUp=readFile("opTreeUp.html").trim();
    private static String opTreeLow=readFile("opTreeLow.html");
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/main/")
    public String mainPage() throws IOException {
    	String ret = header;
    	ret+="<img src=\"../main.png\" style=\"width:100%\">\n";
    	ret += footer;
        return ret;
    }
 
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractOperators/")
    public String listAbstractOperators() throws IOException {
    	String ret = header;
    	List<String> l = AbstractOperatorLibrary.getOperators();
    	ret+= "<h2>Abstract Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/abstractOperators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><h2>Add operator:</h2>"
    		+ "<form action=\"/web/abstractOperators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
        return ret;
    }
    

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractOperators/{id}/")
    public String abstractOperatorDescription(@PathParam("id") String id) throws IOException {
    	String ret = header;
    	ret+= "<h2>Abstract Operator: "+id+"</h2>";

    	ret+= opTreeUp+"\"/abstractOperators/json/"+id+"\";"+opTreeLow;
    	

    	ret+="<form action=\"/web/abstractOperators/checkMatches\" method=\"get\">"
			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
			+ "<input class=\"styled-button\" type=\"submit\" value=\"Check matches\"></form><br>";
    	
    	ret += "<form action=\"/web/abstractOperators/editOperator\" method=\"get\">"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\">"+AbstractOperatorLibrary.getOperatorDescription(id)+"</textarea>"
			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Edit operator\"></form><br>";


    	ret+="<form action=\"/web/abstractOperators/deleteOperator\" method=\"get\">"
			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
			+ "<input class=\"styled-button\" type=\"submit\" value=\"Delete operator\"></form>";
    	//ret += "<p>"+AbstractOperatorLibrary.getOperatorDescription(id)+"</p>";

    	
    	ret += "</div>"+ footer;
        return ret;
    }


    @GET
    @Path("/abstractOperators/checkMatches/")
    @Produces(MediaType.TEXT_HTML)
    public String checkAbstractOperatorMatches(
            @QueryParam("opname") String opname) throws IOException {
    	String ret = header;
    	ret+= "<h2>Matches for: "+opname+"</h2>";
    	List<Operator> l = OperatorLibrary.getMatches(AbstractOperatorLibrary.getOperator(opname));
    	ret += "<ul>";
    	for(Operator op : l){
			ret+= "<li><a href=\"/web/operators/"+op.opName+"\">"+op.opName+"</a></li>";
    		
    	}
    	ret+="</ul>";
    	ret += footer;
    	return ret;
    }

    @GET
    @Path("/abstractOperators/editOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String editAbstractOperator(@QueryParam("opname") String opname,@QueryParam("opString") String opString) throws IOException {
    	String ret = header;
    	AbstractOperatorLibrary.deleteOperator(opname);
    	AbstractOperatorLibrary.addOperator(opname, opString);
    	List<String> l = AbstractOperatorLibrary.getOperators();
    	ret+= "<h2>Abstract Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/abstractOperators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/abstractOperators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }
    
    @GET
    @Path("/abstractOperators/deleteOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String deleteAbstractOperator(
            @QueryParam("opname") String opname) throws IOException {
    	String ret = header;
    	AbstractOperatorLibrary.deleteOperator(opname);
    	List<String> l = AbstractOperatorLibrary.getOperators();
    	ret+= "<h2>Abstract Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/abstractOperators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/abstractOperators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }
    


    @GET
    @Path("/abstractOperators/addOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String addAbstractOperator(
            @QueryParam("opname") String opname,
            @QueryParam("opString") String opString) throws IOException {
    	String ret = header;
    	AbstractOperatorLibrary.addOperator(opname, opString);
    	List<String> l = AbstractOperatorLibrary.getOperators();
    	ret+= "<h2>Abstract Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/abstractOperators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/abstractOperators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }
    
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/operators/")
    public String listOperators() throws IOException {
    	String ret = header;
    	List<String> l = OperatorLibrary.getOperators();
    	ret+= "<h2>Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><h2>Add operator:</h2>"
    		+ "<form action=\"/web/operators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
        return ret;
    }
    

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/operators/{id}/")
    public String operatorDescription(@PathParam("id") String id) throws IOException {
    	String ret = header;
    	ret+= "<h2>Operator: "+id+"</h2><br>";
    	ret+="<form action=\"/web/operators/operatorProfile\" method=\"get\">"
    			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
    			+ "Profile variable:<select name=\"variable\">";
    	for(String outvar : OperatorLibrary.getOperator(id).outputSpace.keySet()){
    		ret+= "<option value=\""+outvar+"\">"+outvar+"</option>";
    	}
		ret+= "</select><br>"
    		+ "<input class=\"styled-button\" type=\"submit\" name=\"profileType\" value=\"Compare models\">"
    		//+ "<input class=\"styled-button\" type=\"submit\" name=\"profileType\" value=\"View model\">"
			+ "<input class=\"styled-button\" type=\"submit\" name=\"profileType\" value=\"View samples\"></form><br>";

    	ret+= opTreeUp+"\"/operators/json/"+id+"\";"+opTreeLow;
    		
    	ret += "<form action=\"/web/operators/editOperator\" method=\"get\">"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\">"+OperatorLibrary.getOperatorDescription(id)+"</textarea>"
			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
			+ "<input class=\"styled-button\" type=\"submit\" value=\"Edit operator\"></form><br>";
    	
    	
    	//ret += "<p>"+OperatorLibrary.getOperatorDescription(id)+"</p>";

    	ret+="<form action=\"/web/operators/deleteOperator\" method=\"get\">"
			+ "<input type=\"hidden\" name=\"opname\" value=\""+id+"\">"
			+ "<input class=\"styled-button\" type=\"submit\" value=\"Delete operator\"></form></div>";

    	
    	ret += footer;
        return ret;
    }


    @GET
    @Path("/operators/operatorProfile/")
    @Produces(MediaType.TEXT_HTML)
    public String operatorProfile(@QueryParam("opname") String opname,@QueryParam("variable") String variable, @QueryParam("profileType") String profileType) throws Exception {
    	String csv = OperatorLibrary.getProfile(opname, variable,profileType);
    	//csv="/mahout_kmeans_synth.csv";
    	String ret = header;

    	ret+= "<h2>Operator profile: "+opname+"</h2>";
    	ret+=scatterPlot.replace("$$", csv)+ footer;
    	return ret;
    }
    
    @GET
    @Path("/operators/editOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String editOperator(@QueryParam("opname") String opname,@QueryParam("opString") String opString) throws Exception {
    	String ret = header;
    	OperatorLibrary.editOperator(opname, opString);
    	List<String> l = OperatorLibrary.getOperators();
    	ret+= "<h2>Operators</h2>";
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/operators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }

    @GET
    @Path("/operators/deleteOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String deleteOperator(
            @QueryParam("opname") String opname) throws IOException {
    	String ret = header;
    	ret+= "<h2>Operators</h2>";
    	OperatorLibrary.deleteOperator(opname);
    	List<String> l = OperatorLibrary.getOperators();
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/operators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }

    @GET
    @Path("/operators/addOperator/")
    @Produces(MediaType.TEXT_HTML)
    public String addOperator(
            @QueryParam("opname") String opname,
            @QueryParam("opString") String opString) throws Exception {
    	String ret = header;
    	ret+= "<h2>Operators</h2>";
    	OperatorLibrary.addOperator(opname, opString);
    	List<String> l = OperatorLibrary.getOperators();
    	ret += "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"/web/operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/operators/addOperator\" method=\"get\">"
			+ "Operator name: <input type=\"text\" name=\"opname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"opString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add operator\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }
    
    

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/datasets/")
    public String listDatasets() throws IOException {
    	String ret = header;
    	ret+= "<h2>Datasets</h2>";
    	List<String> l = DatasetLibrary.getDatasets();
    	ret += "<ul>";
    	for(String d : l){
			ret+= "<li><a href=\"/web/datasets/"+d+"\">"+d+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><h2>Add dataset:</h2>"
    		+ "<form action=\"/web/datasets/addDataset\" method=\"get\">"
			+ "Dataset name: <input type=\"text\" name=\"dname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"dString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add dataset\"></form></div>";
    	
    	ret += footer;
        return ret;
    }
    

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/datasets/{id}/")
    public String datasetDescription(@PathParam("id") String id) throws IOException {
    	String ret = header;
    	ret+= "<h2>Dataset: "+id+"</h2>";
    	ret+= opTreeUp+"\"/datasets/json/"+id+"\";"+opTreeLow;
    	ret += "<form action=\"/web/datasets/editDataset\" method=\"get\">"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"dString\">"+DatasetLibrary.getDatasetDescription(id)+"</textarea>"
			+ "<input type=\"hidden\" name=\"dname\" value=\""+id+"\">"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Edit dataset\"></form></div>";

    	ret+="<div class=\"mainpage\"><form action=\"/web/datasets/deleteDataset\" method=\"get\">"
			+ "<input type=\"hidden\" name=\"dname\" value=\""+id+"\">"
			+ "<input class=\"styled-button\" type=\"submit\" value=\"Delete dataset\"></form></div>";
    	
    	ret += footer;
        return ret;
    }

    @GET
    @Path("/datasets/editDataset/")
    @Produces(MediaType.TEXT_HTML)
    public String editDataset(@QueryParam("dname") String dname,@QueryParam("dString") String dString) throws IOException {
    	String ret = header;
    	ret+= "<h2>Datasets</h2>";
    	DatasetLibrary.deleteDataset(dname);
    	DatasetLibrary.addDataset(dname, dString);
    	List<String> l = DatasetLibrary.getDatasets();
    	ret += "<ul>";
    	for(String d : l){
			ret+= "<li><a href=\"/web/datasets/"+d+"\">"+d+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/datasets/addDataset\" method=\"get\">"
			+ "Dataset name: <input type=\"text\" name=\"dname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"dString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add dataset\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }

    @GET
    @Path("/datasets/deleteDataset/")
    @Produces(MediaType.TEXT_HTML)
    public String deleteDataset(
            @QueryParam("dname") String dname) throws IOException {
    	String ret = header;
    	ret+= "<h2>Datasets</h2>";
    	DatasetLibrary.deleteDataset(dname);
    	List<String> l = DatasetLibrary.getDatasets();
    	ret += "<ul>";
    	for(String d : l){
			ret+= "<li><a href=\"/web/datasets/"+d+"\">"+d+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/datasets/addDataset\" method=\"get\">"
			+ "Dataset name: <input type=\"text\" name=\"dname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"dString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add dataset\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }

    @GET
    @Path("/datasets/addDataset/")
    @Produces(MediaType.TEXT_HTML)
    public String addDataset(
            @QueryParam("dname") String dname,
            @QueryParam("dString") String dString) throws IOException {
    	String ret = header;
    	ret+= "<h2>Datasets</h2>";
    	DatasetLibrary.addDataset(dname, dString);
    	List<String> l = DatasetLibrary.getDatasets();
    	ret += "<ul>";
    	for(String d : l){
			ret+= "<li><a href=\"/web/datasets/"+d+"\">"+d+"</a></li>";
    		
    	}
    	ret+="</ul>";

    	ret+="<div><form action=\"/web/datasets/addDataset\" method=\"get\">"
			+ "Dataset name: <input type=\"text\" name=\"dname\"><br>"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"dString\"></textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Add dataset\"></form></div>";
    	
    	ret += footer;
    	return ret;
    }

    

    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/runningWorkflows/")
    public String listRunningWorkflows() throws IOException {
    	String ret = header;
    	ret+= "<h2>Running Workflows</h2>";
    	ret += "<ul>";

    	List<String> l = RunningWorkflowLibrary.getWorkflows();
    	ret += "<ul>";
    	for(String w : l){
			ret+= "<li><a href=\"/web/runningWorkflows/"+w+"\">"+w+"</a></li>";
    		
    	}
    	ret+="</ul>\n";
    	ret += footer;
        return ret;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/runningWorkflows/{id}/")
    public String runningWorkflowDescription(@PathParam("id") String id) throws IOException {
    	String trackingUrl = RunningWorkflowLibrary.getTrackingUrl(id);
    	String ret = header+
    			"Tracking URL: <a id=\"trackingURL\" href=\""+trackingUrl+"\">"+trackingUrl+"</a>"+
    			"<p id=\"state\">State: "+RunningWorkflowLibrary.getState(id)+"</p>";
    	ret+="</div><div  class=\"mainpage\">";
    	
    	ret+=runningWorkflowUp+"/runningWorkflows/"+id+runningWorkflowLow;
    	ret += footer;
    	return ret;
    }
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/workflows/{id}/")
    public String workflowDescription(@PathParam("id") String id) throws IOException {

    	String ret = header;
    	ret+="</div><div  class=\"mainpage\">";
    	
    	ret+=workflowUp+"/workflows/"+id+workflowLow;

    	ret+="<form action=\"/web/workflows/execute\" method=\"get\">"
    		+ "<input type=\"hidden\" name=\"workflowName\" value=\""+id+"\">"
			+ "<p align=\"right\"><input  class=\"styled-button\" type=\"submit\" value=\"Execute Workflow\"></form>";
    	
    	ret += footer;
    	return ret;
    }
    


    @GET
    @Path("/workflows/execute/")
    @Produces(MediaType.TEXT_HTML)
    public String executeWorkflow(@QueryParam("workflowName") String workflowName) throws Exception{
    	RunningWorkflowLibrary.executeWorkflow(MaterializedWorkflowLibrary.get(workflowName));
    	String trackingUrl = RunningWorkflowLibrary.getTrackingUrl(workflowName);
    	String ret = header+
    			"Tracking URL: <a id=\"trackingURL\" href=\""+trackingUrl+"\">"+trackingUrl+"</a>"+
    			"<p id=\"state\">State: "+RunningWorkflowLibrary.getState(workflowName)+"</p>";
    	ret+="</div><div  class=\"mainpage\">";
    	
    	ret+=runningWorkflowUp+"/runningWorkflows/"+workflowName+runningWorkflowLow;
    	
    	ret += footer;
    	return ret;
    }
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/workflows/")
    public String listWorkflows() throws IOException {
    	String ret = header;
    	ret+= "<h2>Workflows</h2>";
    	ret += "<ul>";

    	List<String> l = MaterializedWorkflowLibrary.getWorkflows();
    	ret += "<ul>";
    	for(String w : l){
			ret+= "<li><a href=\"/web/workflows/"+w+"\">"+w+"</a></li>";
    		
    	}
    	ret+="</ul>\n";
    	ret += footer;
        return ret;
    }


    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractWorkflows/")
    public String listAbstractWorkflows() throws IOException {
    	String ret = header;

    	ret+= "<h2>Abstract Workflows</h2>";
    	List<String> l = AbstractWorkflowLibrary.getWorkflows();
    	ret += "<ul>";
    	for(String w : l){
			ret+= "<li><a href=\"/web/abstractWorkflows/"+w+"\">"+w+"</a></li>";
    		
    	}
    	ret+="</ul>\n";

    	ret+="</div>";

    	ret+="<div  class=\"mainpage\"><p><form action=\"/web/abstractWorkflows/newWorkflow\" method=\"get\">"
    		+ "<p>Name: <input type=\"text\" name=\"workflowName\"></p>"
			+ "<p><input class=\"styled-button\" type=\"submit\" value=\"New Workflow\"></form></p>";
    	ret += footer;
        return ret;
    }

    private String abstractWorkflowView(String workflowName) throws IOException{

    	String ret = header+abstractWorkflowUp+"/abstractWorkflows/"+workflowName+workflowLow;
    	ret+="</div>";

    	ret+="<div  class=\"mainpage\"><p><form action=\"/web/abstractWorkflows/materialize\" method=\"get\">"
			+ "Policy: <p><input type=\"hidden\" name=\"workflowName\" value=\""+workflowName+"\">"
			+ "<textarea rows=\"4\" cols=\"80\" name=\"policy\">"+defaultPolicy()+"</textarea></p>"
			+ "<p><input class=\"styled-button\" type=\"submit\" value=\"Materialize Workflow\"></form></p>";
    	
    	ret+="<p><form action=\"/web/abstractWorkflows/addRemove\" method=\"get\">"
    			+ "Comma separated list: <textarea rows=\"1\" cols=\"80\" name=\"name\"></textarea><br>"
    			+ "<p><input type=\"radio\" name=\"type\" value=\"1\" checked>Abstract Operator<br>"
    			+ "<input type=\"radio\" name=\"type\" value=\"2\">Materialized Operator<br>"
    			+ "<input type=\"radio\" name=\"type\" value=\"3\">Abstract Dataset<br>"
    			+ "<input type=\"radio\" name=\"type\" value=\"4\">Materialized Dataset<br>"
    			+ "<input type=\"hidden\" name=\"workflowName\" value=\""+workflowName+"\"></p>"
    			+ "<p><input class=\"styled-button\" name=\"addRemove\" type=\"submit\" value=\"Add nodes\">"
    			+ "<input class=\"styled-button\" name=\"addRemove\" type=\"submit\" value=\"Remove nodes\"></form></p>";
    	
    	ret+="<p><form action=\"/web/abstractWorkflows/changeGraph\" method=\"get\">"
    		+ "<input type=\"hidden\" name=\"workflowName\" value=\""+workflowName+"\">"
			+ "<textarea rows=\"40\" cols=\"150\" name=\"workflowGraph\">"+AbstractWorkflowLibrary.getGraphDescription(workflowName)+"</textarea>"
			+ "<br><input class=\"styled-button\" type=\"submit\" value=\"Change graph\"></form></p>";
    	
    	
    	ret += footer;
    	return ret;
    }
    
    protected String defaultPolicy() {
    	String ret ="metrics,cost,execTime\n";
    	ret+="groupInputs,execTime,max\n";
    	ret+="groupInputs,cost,sum\n";
    	ret+="function,execTime,min";
    	return ret;
	}
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractWorkflows/newWorkflow/")
    public String newWorkflow(@QueryParam("workflowName") String workflowName) throws IOException {
    	AbstractWorkflowLibrary.newWorkflow(workflowName);
        return abstractWorkflowView(workflowName);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractWorkflows/changeGraph/")
    public String changeAbstractWorkflowDescription(@QueryParam("workflowName") String workflowName, @QueryParam("workflowGraph") String workflowGraph) throws IOException {
    	logger.info("workflowName: "+workflowName);
    	logger.info("workflowGraph: "+workflowGraph);
    	
    	AbstractWorkflowLibrary.changeWorkflow(workflowName, workflowGraph);
    	
        return abstractWorkflowView(workflowName);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractWorkflows/addRemove/")
    public String addNodeToWorkflow(@QueryParam("workflowName") String workflowName, @QueryParam("type") String type, @QueryParam("name") String name, 
    		@QueryParam("addRemove") String addRemove) throws IOException {
    	if(name.isEmpty() || type.isEmpty() || workflowName.isEmpty())
            return abstractWorkflowView(workflowName);
    		
    	if(addRemove.contains("Add nodes")){
	    	String[] names = name.split(",");
	    	for (int i = 0; i < names.length; i++) {
	    		if(!names[i].isEmpty())
	    			AbstractWorkflowLibrary.addNode(workflowName, type, names[i]);
			}
    	}
    	else if (addRemove.contains("Remove nodes")){
	    	String[] names = name.split(",");
	    	for (int i = 0; i < names.length; i++) {
	    		if(!names[i].isEmpty())
	    			AbstractWorkflowLibrary.removeNode(workflowName, type, names[i]);
			}
    	}
        return abstractWorkflowView(workflowName);
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/abstractWorkflows/{workflowName}/")
    public String abstractWorkflowDescription(@PathParam("workflowName") String workflowName) throws IOException {
    	AbstractWorkflowLibrary.refresh(workflowName);
        return abstractWorkflowView(workflowName);
    }
    

    @GET
    @Path("/abstractWorkflows/materialize/")
    @Produces(MediaType.TEXT_HTML)
    public String materializeAbstractWorkflow(@QueryParam("workflowName") String workflowName,
    		@QueryParam("policy") String policy) throws Exception{
    	AbstractWorkflowLibrary.refresh(workflowName);
    	String mw = AbstractWorkflowLibrary.getMaterializedWorkflow(workflowName,policy);
    	String ret = header+"Optimal result for policy function: <br>"+AbstractWorkflow1.getPolicyFromString(policy)+" = "+MaterializedWorkflowLibrary.get(mw).optimalCost;
    	ret+="</div><div  class=\"mainpage\">";
    	
    	ret+=workflowUp+"/workflows/"+mw+workflowLow;
    	
    	ret+="<form action=\"/web/workflows/execute\" method=\"get\">"
        		+ "<input type=\"hidden\" name=\"workflowName\" value=\""+mw+"\">"
    			+ "<p align=\"right\"><input  class=\"styled-button\" type=\"submit\" value=\"Execute Workflow\"></form>";
        	
    	
    	ret += footer;
    	return ret;
    }
    



    
    private static String readFile(String name){
    	InputStream stream = Main.class.getClassLoader().getResourceAsStream(name);
        if (stream == null) {
            System.err.println("No "+name+" file was found! Exiting...");
            System.exit(1);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        try {
			while ((line = reader.readLine()) != null) {
			    out.append(line);
			    out.append(newLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return out.toString();
    }

    
}
