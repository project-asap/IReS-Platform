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


package gr.ntua.cslab.asap.staticLibraries;


import gr.ntua.cslab.asap.operators.Operator;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class ClusterStatusLibrary {
	public static ConcurrentHashMap< String, Boolean> status;
	public static ConcurrentHashMap< String, String> cluster_static_resources = null;
	public static ConcurrentHashMap< String, String> cluster_available_resources = null;

	public static void initialize() throws Exception{
		status = new ConcurrentHashMap<String,Boolean>();
		status.put("WEKA", true);
		status.put("Spark", true);
		status.put("MapReduce", true);
		status.put( "MLLib", true);
		status.put( "Python", true);
		ClusterStatusLibrary.cluster_static_resources = new ConcurrentHashMap< String, String>();
		ClusterStatusLibrary.cluster_available_resources = new ConcurrentHashMap< String, String>();
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
