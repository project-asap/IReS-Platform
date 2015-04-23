package gr.ntua.cslab.asap.staticLibraries;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDescription;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.jeval.function.string.Length;

import org.apache.log4j.Logger;

public class OperatorLibrary {
	private static HashMap<String,Operator> operators;
	public static String operatorDirectory;
	private static Logger logger = Logger.getLogger(OperatorLibrary.class.getName());
	
	public static void initialize(String directory) throws Exception{
		operatorDirectory = directory;
		operators = new HashMap<String,Operator>();
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
		        Logger.getLogger(OperatorLibrary.class.getName()).info("Loading operator: " + listOfFiles[i].getName());
				Operator temp = new Operator(listOfFiles[i].getName(), listOfFiles[i].toString());
				temp.readFromDir();
				operators.put(temp.opName, temp);
		    }
		}
	}
	
	public static void refresh(){
		
	}
	
	public static List<String> getOperators(){
		List<String> ret = new ArrayList<String>();
		for(Operator op : operators.values()){
			ret.add(op.opName);
		}
		return ret;
	}
	
	public static List<Operator> getMatches(AbstractOperator abstractOperator){
		//logger.info("Check matches: "+abstractOperator.opName);
		List<Operator> ret = new ArrayList<Operator>();
		for(Operator op : operators.values()){
			if(abstractOperator.checkMatch(op))
				ret.add(op);
		}
		//for(Operator o :ret){
		//	logger.info("Found: "+o.opName);
		//}
		return ret;
	}
	
	public static List<Operator> checkMove(Dataset from, Dataset to) {
		//logger.info("Check move from: "+from+" to: "+to);
		AbstractOperator abstractMove = new AbstractOperator("move");
		abstractMove.moveOperator(from,to);
		return getMatches(abstractMove);
	}

	public static String getOperatorDescription(String id) {
		Operator op = operators.get(id);
		if(op==null)
			return "No description available";
		return op.toKeyValues("\n");
	}

	public static OperatorDescription getOperatorDescriptionJSON(String id) {
		Operator op = operators.get(id);
		if(op==null)
			return new OperatorDescription("", "");
		return op.toOperatorDescription();
	}
	
	public static void add(Operator o) {
		operators.put(o.opName, o);
	}

	public static void editOperator(String opname, String opString) throws Exception {
		String dir = "asapLibrary/operators/"+opname;
		Operator old = operators.remove(opname);
		old.writeModels(dir);
		
    	Operator o = new Operator(opname,dir);
    	InputStream is = new ByteArrayInputStream(opString.getBytes());
    	o.readPropertiesFromStream(is);
    	o.writeDescriptionToPropertiesFile(dir);

		Operator temp = new Operator(opname, dir);
		temp.readFromDir();
		operators.put(temp.opName, temp);
	}
	
	public static void addOperator(String opname, String opString) throws Exception {
    	Operator o = new Operator(opname,"asapLibrary/operators/"+opname);
    	InputStream is = new ByteArrayInputStream(opString.getBytes());
    	o.readPropertiesFromStream(is);
    	o.writeToPropertiesFile("asapLibrary/operators/"+o.opName);
    	o.configureModel();
    	add(o);
    	is.close();
	}

	public static void deleteOperator(String opname) {
		Operator op = operators.remove(opname);
		op.deleteDiskData();
	}
	

	public static Operator getOperator(String opname) {
		return operators.get(opname);
	}

	public static String getProfile(String opname, String variable, String profileType) throws Exception {
		Operator op = operators.get(opname);
		op.configureModel();

    	if(profileType.equals("Compare models")){
    		File csv  = new File(operatorDirectory+"/"+op.opName+"/data/"+variable+".csv");
    		if(csv.exists()){
    			op.writeCSVfileUniformSampleOfModel(variable, 1.0, "www/test.csv", ",",true);
    			append("www/test.csv",csv.toString(),",", op.inputSpace);
    		}
    		else{
    			op.writeCSVfileUniformSampleOfModel(variable, 1.0, "www/test.csv", ",",false);
    		}
    	}
    	else if(profileType.equals("View model")){
			op.writeCSVfileUniformSampleOfModel(variable, 1.0, "www/test.csv", ",",false);
    	}
    	else{
    		File csv  = new File(operatorDirectory+"/"+op.opName+"/data/"+variable+".csv");
    		if(csv.exists()){
				op.writeCSVfileUniformSampleOfModel(variable, 0.0, "www/test.csv", ",",true);
				append("www/test.csv",csv.toString(),",", op.inputSpace);
    		}
    		else{
				op.writeCSVfileUniformSampleOfModel(variable, 0.0, "www/test.csv", ",",true);
    		}
    	}
		//op.writeCSVfileUniformSampleOfModel(1.0, "www/test.csv", ",");
		return "/test.csv";
		/*if(opname.equals("Sort"))
			return "/terasort.csv";
		else
			return "/iris.csv";*/
			
	}

	private static void append(String toCSV, String fromCSV,String delimiter, HashMap<String, String> inputSpace) throws Exception {

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(toCSV, true)));
        BufferedReader br = new BufferedReader(new FileReader(fromCSV));
        String line = br.readLine();
        String[] variables = line.split(delimiter);
        HashMap<String, Integer> varMapping = new HashMap<String, Integer>();
    	for (int i = 0; i < variables.length; i++) {
    		int j = 0;
            for(String k: inputSpace.keySet()){
				if(variables[i].equals(k)){
					varMapping.put(k, j);
					break;
				}
				j++;
			}
        }
    	//System.out.println(varMapping);
        line = br.readLine();
        while (line != null) {
        	String[] sample = line.split(delimiter);
        	String[] ls = new String[sample.length-1];
        	for (int i = 0; i < sample.length-1; i++) {
        		//System.out.println(variables[i]+" "+sample[i]);
				ls[varMapping.get(variables[i])]= sample[i];
			}
        	String n = "";
        	for(String s : ls){
        		n+=s+delimiter;
        	}
        	n+=sample[sample.length-1]+delimiter;
        	//System.out.println(n);
            out.append(n+"Samples");
            out.append(System.lineSeparator());
            line = br.readLine();
        }
        out.close();
    	br.close();
	}



	/*protected static void writeCSV(String file, ){
		
	}*/
	
}
