package gr.ntua.cslab.asap.staticLibraries;


import gr.ntua.cslab.asap.operators.Operator;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class ClusterStatusLibrary {
	public static ConcurrentHashMap<String,Boolean> status ;

	public static void initialize() throws Exception{
		status = new ConcurrentHashMap<String,Boolean>();
		status.put("WEKA", true);
		status.put("Spark", true);
		status.put("MapReduce", true);
		status.put( "MLLib", true);
		status.put( "Python", true);
	}
	
	public static boolean getStatus(String component){
		return status.get(component);		
	}
	
	public static void setStatus(String component, boolean b){
		status.put(component, b);		
	}

	public static boolean checkEngineStatus(Operator op) {
		String engine = op.getEngine();
		if(engine==null)
			return true;
		Boolean ret = status.get(engine);
		if(ret==null)
			return true;
		else
			return ret;
	}
	
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String listOperators() {
    	String ret = "<ul>";
    	for(Entry<String, Boolean> e : status.entrySet()){
			ret+= "<li>"+e.getKey()+" : "+e.getValue()+"</li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }
}
