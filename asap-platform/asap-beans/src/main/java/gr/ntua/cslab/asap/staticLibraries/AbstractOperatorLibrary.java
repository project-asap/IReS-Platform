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

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDescription;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class AbstractOperatorLibrary {
	private static HashMap<String,AbstractOperator> operators;
	private static String operatorDirectory;
	
	public static void initialize(String directory) throws IOException{
		operatorDirectory = directory;
		operators = new HashMap<String,AbstractOperator>();
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && !listOfFiles[i].isHidden()) {
		        Logger.getLogger(AbstractOperatorLibrary.class.getName()).info("Loading operator: " + listOfFiles[i].getName());
		        AbstractOperator temp = new AbstractOperator(listOfFiles[i].getName());
				temp.readPropertiesFromFile(listOfFiles[i]);
				operators.put(temp.opName, temp);
		    }
		}
	}
	
	public static void refresh(){
		
	}
	
	public static List<String> getOperators(){
		List<String> ret = new ArrayList<String>();
		for(AbstractOperator op : operators.values()){
			ret.add(op.opName);
		}
		return ret;
	}
	
	public static String getOperatorDescription(String id) {
		AbstractOperator op = operators.get(id);
		if(op==null)
			return "No description available";
		return op.toKeyValues("\n");
	}

	public static void add(AbstractOperator o) {
		operators.put(o.opName, o);
	}

	public static void addOperator(String opname, String opString) throws IOException {
		AbstractOperator o = new AbstractOperator(opname);
    	InputStream is = new ByteArrayInputStream(opString.getBytes());
    	o.readPropertiesFromFile(is);
    	o.writeToPropertiesFile("asapLibrary/operators/"+o.opName);
    	add(o);
    	is.close();
	}

	public static void deleteOperator(String opname) {
		AbstractOperator op = operators.remove(opname);
		File file = new File(operatorDirectory+"/"+op.opName);
		file.delete();
	}

	public static AbstractOperator getOperator(String opname) {
		return operators.get(opname);
	}

	public static OperatorDescription getOperatorDescriptionJSON(String id) {
		 AbstractOperator op = operators.get(id);
		if(op==null)
			return new OperatorDescription("", "");
		return op.toOperatorDescription();
	}


}
