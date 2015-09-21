package gr.ntua.cslab.asap.operators;

import gr.ntua.cslab.asap.rest.beans.OperatorDescription;
import gr.ntua.cslab.asap.utils.Utils;
import gr.ntua.cslab.asap.workflow.WorkflowNode;
import gr.ntua.ece.cslab.panic.core.classifier.UserFunctionClassifier;
import gr.ntua.ece.cslab.panic.core.client.Benchmark;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.AbstractWekaModel;
import gr.ntua.ece.cslab.panic.core.models.MLPerceptron;
import gr.ntua.ece.cslab.panic.core.models.Model;
import gr.ntua.ece.cslab.panic.core.models.UserFunction;
import gr.ntua.ece.cslab.panic.core.samplers.Sampler;
import gr.ntua.ece.cslab.panic.core.samplers.UniformSampler;
import gr.ntua.ece.cslab.panic.core.utils.CSVFileManager;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;


public class Operator {
	public HashMap<String,List<Model>> models;
	public HashMap<String, String> inputSpace, outputSpace;
	public SpecTree optree;
	public String opName;
	private static Logger logger = Logger.getLogger(Operator.class.getName());
	public String directory;
	
	public Operator(String name, String directory) {
		optree = new SpecTree();
		opName = name;
		models = new HashMap<String, List<Model>>();
		this.directory=directory;
	}
	

	/*public void readModel(File file) throws Exception {
		String modelClass = optree.getParameter("Optimization.model");
		if(modelClass==null){
			performanceModel = AbstractWekaModel.readFromFile(file.toString()+"/model");
		}
	}*/
	
	/**
	 * @throws Exception
	 */
	public void configureModel() throws Exception {

		inputSpace = new HashMap<String, String>();
		optree.getNode("Optimization.inputSpace").toKeyValues("", inputSpace );
		outputSpace = new HashMap<String, String>();
		optree.getNode("Optimization.outputSpace").toKeyValues("", outputSpace );
		
		for(Entry<String, String> e : outputSpace.entrySet()){
			List<Model> performanceModels = new ArrayList<Model>();
			String modelClass = optree.getParameter("Optimization.model."+e.getKey());
			//System.out.println(e.getKey()+" class: "+modelClass);
			if(modelClass.contains("AbstractWekaModel")){
				String modelDir = directory+"/models";
				File modelFile = new File(modelDir);
				if(modelFile.exists()){

					File[] listOfFiles = modelFile.listFiles();

					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].toString().endsWith(".model")) {
							//System.out.println("Reading: "+listOfFiles[i].getAbsolutePath());
							performanceModels.add(AbstractWekaModel.readFromFile(listOfFiles[i].getAbsolutePath()));
						}
					}
				}
				else{
					modelFile.mkdir();
					int i=0;
					for (Class<? extends Model> c : Benchmark.discoverModels()) {
						if(c.equals(UserFunction.class))
							continue;
						//System.out.println(c);
	                	Model model = (Model) c.getConstructor().newInstance();

						CSVFileManager file = new CSVFileManager();
			            file.setFilename(directory+"/data/"+e.getKey()+".csv");
			            
			            //build models with adaptive sampling
						/*
						Sampler s = (Sampler) new UniformSampler();
			            
			            // samplers initialization
			            s.setSamplingRate(0.8);
			            //System.out.println(file.getDimensionRanges());
			            s.setDimensionsWithRanges(file.getDimensionRanges());
	
			            s.configureSampler();
			            while (s.hasMore()) {
			                InputSpacePoint nextSample = s.next();
			                OutputSpacePoint out = file.getActualValue(nextSample);
			                //System.out.println(out);
			                model.feed(out, false);
			            }
			            */
			            
			            
			            //build models without adaptive sampling
			            for(InputSpacePoint in : file.getInputSpacePoints()){
			                OutputSpacePoint out = file.getActualValue(in);
			                //System.out.println(out);
			                model.feed(out, false);
			            }
			            

			            try{
			            	model.train();
			            }catch(Exception e1){
			            	continue;
			            }
			            model.serialize(modelDir+"/"+e.getKey()+"_"+i+".model");
			            i++;
			            performanceModels.add(model);
		            }
				}
			}
			else{
				performanceModels.add((Model) Class.forName(modelClass).getConstructor().newInstance());
			}
			
			for(Model performanceModel : performanceModels){
				performanceModel.setInputSpace(inputSpace);
				HashMap<String, String> temp = new HashMap<String, String>();
				temp.put(e.getKey(), e.getValue());
				performanceModel.setOutputSpace(temp);
	
				HashMap<String, String> conf = new HashMap<String, String>();
				optree.getNode("Optimization").toKeyValues("", conf );
				//System.out.println("sadfas: "+conf);
				performanceModel.configureClassifier(conf);
			}
			models.put(e.getKey(), performanceModels);
		}
	}


	public void writeCSVfileUniformSampleOfModel(String variable, Double samplingRate, String filename, String delimiter, boolean addPredicted) throws Exception{

        File file = new File(filename);
    	FileOutputStream fos = new FileOutputStream(file);
    	 
    	BufferedWriter writter = new BufferedWriter(new OutputStreamWriter(fos));
    	getUniformSampleOfModel(variable, samplingRate, writter, delimiter, addPredicted);

    	writter.close();
		
	}
	
	public void writeCSVfileUniformSampleOfModel(String variable, Double samplingRate, String filename, String delimiter) throws Exception{

        File file = new File(filename);
    	FileOutputStream fos = new FileOutputStream(file);
    	 
    	BufferedWriter writter = new BufferedWriter(new OutputStreamWriter(fos));
    	getUniformSampleOfModel(variable, samplingRate, writter, delimiter, false);

    	writter.close();
	}
	
	protected void getUniformSampleOfModel(String variable, Double samplingRate, BufferedWriter writter, String delimiter, boolean addPredicted) throws Exception{
		
		List<Model> lm = models.get(variable);
		Model m1 = lm.get(0); 
    	HashMap<String, List<Double>> dim = new HashMap<String, List<Double>>();
        for(Entry<String, String> e : m1.getInputSpace().entrySet()){
            writter.append(e.getKey()+delimiter);
        	String[] limits = e.getValue().split(delimiter);
        	List<Double> l = new ArrayList<Double>();
        	Double min = Double.parseDouble(limits[1]);
        	Double max = Double.parseDouble(limits[2]);
        	if(limits[3].startsWith("l")){
        		Double step = 10.0;
        		for (double i = min; i <= max; i*=step) {
        			l.add(i);
				}
        	}
        	else{
        		Double step = Double.parseDouble(limits[3]);
        		for (double i = min; i <= max; i+=step) {
        			l.add(i);
				}
        	}
        	dim.put(e.getKey(), l);
        }
        int i=0;
        for(String k : m1.getOutputSpace().keySet()){
            writter.append(k);
        	i++;
        	if(i<m1.getOutputSpace().size()){
                writter.append(delimiter);
        	}
        }
        if(addPredicted){
            writter.append(delimiter+"model");
        }
        writter.newLine();

		for(Model m : lm){
	        //System.out.println(dim);
			Sampler s = (Sampler) new UniformSampler();
	        s.setSamplingRate(samplingRate);
			s.setDimensionsWithRanges(dim);
	        s.configureSampler();
	        while (s.hasMore()) {
	            InputSpacePoint nextSample = s.next();
	    		OutputSpacePoint op =  new 	OutputSpacePoint();
	            HashMap<String, Double> values = new HashMap<String, Double>();
	            for(String k :  m.getOutputSpace().keySet()){
	            	values.put(k, null);
	            }
	            op.setValues(values);
	            //System.out.println(nextSample);
	            OutputSpacePoint res = m.getPoint(nextSample,op);
	            //System.out.println(res);
	            writter.append(res.toCSVString(delimiter));
	            if(addPredicted){
	                writter.append(delimiter+m.getClass().getSimpleName());
	            }
	            writter.newLine();
	        }
		}
        
	}
	
	public void add(String key, String value) {
        //Logger.getLogger(Operator.class.getName()).info("Adding key: "+key+" value: "+value);
		optree.add(key,value);
	}
	
	@Override
	public String toString() {
		String ret = opName+": ";
		ret+= optree.toString();
		return ret;
	}
	
	public String toKeyValues(String separator) {
		String ret ="";
		ret+=optree.toKeyValues("", ret,separator);
		return ret;
	}
	

	public OperatorDescription toOperatorDescription() {
		OperatorDescription ret = new OperatorDescription(opName, "");
		optree.toOperatorDescription(ret);
		return ret;
	}
	
	public void readFromDir() throws Exception{
		//System.out.println("operator: "+opName);
        File f = new File(directory+"/description");
		InputStream stream = new FileInputStream(f);
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
		stream.close();
		configureModel();
		
		//this.performanceModel = AbstractWekaModel.readFromFile(directory+"/model");
	}


	public void readPropertiesFromString(String properties) throws IOException {
		InputStream stream = new ByteArrayInputStream(properties.getBytes());
		readPropertiesFromStream(stream);
		stream.close();
	}
	
	public void readPropertiesFromStream(InputStream stream) throws IOException {
		Properties props = new Properties();
		props.load(stream);
		for(Entry<Object, Object> e : props.entrySet()){
			add((String)e.getKey(), (String)e.getValue());
		}
	}

	private void copyExecPath(Dataset d, String path){
		if(path!=null){
			if(path.startsWith("$HDFS_OP_DIR")){
				String newPath = path.replace("$HDFS_OP_DIR", "$HDFS_DIR/"+opName);
				d.add("Execution.path", newPath);
			}
			else{
				d.add("Execution.path", opName+"/"+path);
			}
		}
	}
	
	public void copyExecVariables(Dataset d, int position, List<WorkflowNode> inputs){
		SpecTreeNode variables = optree.getNode("Execution.Output"+position);
		HashMap<String, String> val = new HashMap<String, String>();
		variables.toKeyValues("", val);
		for(Entry<String, String> e: val.entrySet()){
			if(e.getKey().equals("path")){
				copyExecPath(d, e.getValue());
			}
			else{
	    		String[] s = e.getValue().split("\\.");
				if(s[0].startsWith("In")){
	    			int index = Integer.parseInt(s[0].substring((s[0].length()-1)));
	    			//System.out.println("data index "+ index);
		    		WorkflowNode n = inputs.get(index);
		    		String v = "";
		    		if(n.isOperator)
		    			v=n.inputs.get(0).dataset.getParameter("Execution."+s[1]);
		    		else
		    			v = n.dataset.getParameter("Execution."+s[1]);
		    		if(v==null){
		    			v ="_";
		    		}
					d.add("Execution."+e.getKey(), v);
				}
				else{
					d.add("Execution."+e.getKey(), e.getValue());
				}
			}
		}
	}
	
	public void outputFor(Dataset d, int position, List<WorkflowNode> inputs) throws Exception {
		//System.out.println("Generating output for pos: "+ position);
		d.datasetTree = optree.copyInputSubTree("Constraints.Output"+position);
		if(d.datasetTree == null)
			d.datasetTree = new SpecTree();
		
		copyExecVariables(d,position,inputs);
		generateOptimizationMetrics(d,position,inputs);
		/*int min = Integer.MAX_VALUE;
		for(WorkflowNode n :inputs){
			int temp = Integer.MAX_VALUE;
			if(!n.inputs.get(0).isOperator)
				temp= Integer.parseInt(n.inputs.get(0).dataset.getParameter("Optimization.uniqueKeys"));
			else
				 temp = Integer.parseInt(n.inputs.get(0).inputs.get(0).dataset.getParameter("Optimization.uniqueKeys"));
			if(temp<min){
				min=temp;
			}
		}
		d.datasetTree.add("Optimization.uniqueKeys", min+"");*/
	}
	
	public void generateOptimizationMetrics(Dataset d, int position,List<WorkflowNode> inputs) throws Exception {
		for(String out : outputSpace.keySet()){
			if(out.startsWith("Out"+position)){
	    		String[] s = out.split("\\.");
				d.add("Optimization."+s[1], getMettric(out, inputs)+"");
			}
		}
		
	}


	public void writeToPropertiesFile(String directory) throws Exception {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        Properties props = new Properties();
		optree.writeToPropertiesFile("", props);
        File f = new File(directory+"/description");
        if (f.exists()) {
        	f.delete();
        }
    	f.createNewFile();
        OutputStream out = new FileOutputStream( f );
        props.store(out,"");
        out.close();
        writeModels(directory);
	}

	public void writeDescriptionToPropertiesFile(String directory) throws Exception {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        Properties props = new Properties();
		optree.writeToPropertiesFile("", props);
        File f = new File(directory+"/description");
        if (f.exists()) {
        	f.delete();
        }
    	f.createNewFile();
        OutputStream out = new FileOutputStream( f );
        props.store(out,"");
        out.close();
	}
	
	public void writeModels(String directory) throws Exception {
        File mdir = new File(directory+"/models");
        if (mdir.exists()) {
            mdir.delete();
        }
        mdir.mkdir();
        for(Entry<String, List<Model>> e : models.entrySet()){
        	int i =0;
        	for(Model m : e.getValue()){
        		if(m.getClass().equals(UserFunction.class))
        			continue;
        		m.serialize(directory+"/models/"+e.getKey()+"_"+i+".model");
        		i++;
        	}
        }
	}
	
	
	public Double getMettric(String metric, List<WorkflowNode> inputs) throws Exception{
		logger.info("Getting mettric: "+metric+" from operator: "+opName);
		//System.out.println(metric);
		Model model = models.get(metric).get(0);
		if(!model.getClass().equals(gr.ntua.ece.cslab.panic.core.models.UserFunction.class)){
			for(Model m:models.get(metric)){
				
				if(inputSpace.size()>=2 && m.getClass().equals(gr.ntua.ece.cslab.panic.core.models.MLPerceptron.class)){
					model =m;
					break;
				}
				if(inputSpace.size()<2 && m.getClass().equals(gr.ntua.ece.cslab.panic.core.models.LinearRegression.class)){
					model =m;
					break;
				}
			}
		}
		logger.info("Model selected: "+ model.getClass());
		//System.out.println(opName);
		//System.out.println("inputs: "+inputs);

        InputSpacePoint in = new InputSpacePoint();
        HashMap<String, Double> values = new HashMap<String, Double>();
    	for(String inVar : model.getInputSpace().keySet()){
    		//System.out.println("var: "+inVar);
    		String[] s = inVar.split("\\.");
    		if(s[0].startsWith("In")){
    			int index = Integer.parseInt(s[0].substring((s[0].length()-1)));
    			//System.out.println("data index "+ index);
	    		String val = null;
	    		WorkflowNode n = inputs.get(index);
	    		if(n.isOperator)
	    			val=n.inputs.get(0).dataset.getParameter("Optimization."+s[1]);
	    		else
	    			val = n.dataset.getParameter("Optimization."+s[1]);
	    		if(val==null){
	    			val ="10.0";
	    		}
	    		Double maxVal = Double.parseDouble(inputSpace.get(inVar).split(",")[2]);
	    		
	    		Double v = Double.parseDouble(val);
	    		if(v>maxVal && !model.getClass().equals(gr.ntua.ece.cslab.panic.core.models.UserFunction.class)){
	    			//System.out.println("found: "+v+" : "+maxVal);
	    			Random r = new Random();
	    			return 750+r.nextDouble()*500;
	    		}
    			//System.out.println("in value "+ v);
    			values.put(inVar, v);			
    		}
    		else{
    			//System.out.println("in value "+ 2.0);
    			values.put(inVar, 2.0);			
    			
    		}
    	}
    	in.setValues(values);
        
		OutputSpacePoint op =  new 	OutputSpacePoint();
        values = new HashMap<String, Double>();
        for(String k :  model.getOutputSpace().keySet()){
        	values.put(k, null);
        }
        op.setValues(values);
        //System.out.println(in);
        OutputSpacePoint res = model.getPoint(in,op);
        //System.out.println(res);
        //System.out.println("return: " + res.getOutputPoints().get(metric));
        return res.getOutputPoints().get(metric);
		
	}
	
	public Double getCost(List<WorkflowNode> inputs) throws NumberFormatException, EvaluationException {

		logger.info("Compute cost Operator "+opName);
		logger.info("inputs: "+inputs);
		String value = getParameter("Optimization.execTime");
		logger.info("value "+value);
		Evaluator evaluator = new Evaluator();
		if(value.contains("$")){
			int offset=1;
			if(value.startsWith("\\$"))
				offset=0;
			String[] variables = value.split("\\$");
			List<String> vars = new ArrayList<String>();
			for (int i = 0; i < variables.length; i+=1) {
				logger.info("split "+variables[i]);
			}
			for (int i = offset; i < variables.length; i+=2) {
				vars.add(variables[i]);
			}
			logger.info("Variables: "+vars);
			
			for(String var : vars){
				String[] s = var.split("\\.");
				for (int i = 0; i < s.length; i+=1) {
					logger.info("split "+s[i]);
				}
				
				int inNum = Integer.parseInt(s[0]);
				WorkflowNode n = inputs.get(inNum);
				String val = null;
				if(n.isOperator)
					val=n.inputs.get(0).dataset.getParameter("Optimization."+s[1]);
				else
					val = n.dataset.getParameter("Optimization."+s[1]);
				if(val==null){
					val ="10.0";
				}
				logger.info("Replace: "+"$"+var+"$  "+ val);
				value=value.replace("$"+var+"$", val);
			}
			logger.info("Evaluate value "+value);

			logger.info("Cost: "+evaluator.evaluate(value));
			return Double.parseDouble(evaluator.evaluate(value));
		}
		else{
			logger.info("Cost: "+evaluator.evaluate(value));
			return Double.parseDouble(evaluator.evaluate(value));
		}
	}
	
	/*public Double getCost() {
		String value = getParameter("Optimization.execTime");
		return Double.parseDouble(value);
	}*/

	public String getParameter(String key) {
		return optree.getParameter(key);
	}


	public void deleteDiskData() {
		File file = new File(directory);
		Utils.deleteDirectory(file);
	}
	
	@Override
	protected Operator clone() throws CloneNotSupportedException {
		Operator ret = new Operator(opName, directory);
		ret.optree = optree.clone();
		return ret;
	}



	public static void main(String[] args) throws Exception {
		Operator op = new Operator("HBase_HashJoin","");
		op.add("Constraints.Input.number","2");
		op.add("Constraints.Output.number","1");
		op.add("Constraints.Input0.DataInfo.Attributes.number","2");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input0.Engine.DB.NoSQL.HBase.location","127.0.0.1");

//		op.add("Constraints.Input1.DataInfo.Attributes.number","2");
//		op.add("Constraints.Input1.DataInfo.Attributes.Atr1.type","ByteWritable");
//		op.add("Constraints.Input1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
//		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.key","Atr1");
//		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.value","Atr2");
//		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.location","127.0.0.1");
//
//		op.add("Constraints.Output0.DataInfo.Attributes.number","2");
//		op.add("Constraints.Output0.DataInfo.Attributes.Atr1.type","ByteWritable");
//		op.add("Constraints.Output0.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
//		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.key","Atr1");
//		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.value","Atr2");
//		op.add("Constraints.Output0.Engine.DB.NoSQL.HBase.location","127.0.0.1");
//		
//		op.add("Constraints.OpSpecification.Algorithm.Join.JoinCondition","in1.atr1 = in2.atr2");
//		op.add("Constraints.OpSpecification.Algorithm.Join.type", "HashJoin");
//
//		op.add("Constraints.EngineSpecification.Distributed.MapReduce.masterLocation", "127.0.0.1");
//
//		op.add("Optimization.model", "gr.ntua.ece.cslab.panic.core.models.UserFunction");
//		op.add("Optimization.inputSpace.In0.uniqueKeys", "Double,1.0,1E10,l");
//		op.add("Optimization.inputSpace.In1.uniqueKeys", "Double,1.0,1E10,l");
		op.add("Optimization.inputSpace.cores", "Double,1.0,40.0,5.0");
		op.add("Optimization.outputSpace.execTime", "Double");
		op.add("Optimization.outputSpace.Out0.uniqueKeys", "Integer");
		op.add("Optimization.execTime", "100.0 + (In0.uniqueKeys + In1.uniqueKeys)/cores");
		op.add("Optimization.Out0.uniqueKeys", "In0.uniqueKeys + In1.uniqueKeys");
		
		Operator op1 = op.clone();
		System.out.println(op1.toKeyValues("\n"));
		System.out.println(op1.getParameter("Optimization.Out0.uniqueKeys"));
		op.add("Optimization.Out0.uniqueKeys", "In0.uniqueKeys");
		System.out.println(op1.getParameter("Optimization.Out0.uniqueKeys"));
		System.out.println(op.getParameter("Optimization.Out0.uniqueKeys"));
		System.exit(0);
		
		
		Operator op2 = new Operator("HBase_HashJoin","/Users/npapa/Documents/workspace/asap/asapLibrary/operators/Sort");

		op.readFromDir();
		op.writeCSVfileUniformSampleOfModel("Out0.size", 1.0, "test.csv", ",");
		
		//op.configureModel();
		//op.writeCSVfileUniformSampleOfModel(0.2, "test.csv", ",");
		
		System.exit(0);
		
		/*op.add("Constraints.Input1.DataInfo.Attributes.number","2");
		op.add("Constraints.Input1.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input1.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op.add("Constraints.Input2.DataInfo.Attributes.number","2");
		op.add("Constraints.Input2.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Input2.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Input2.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Input2.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Input2.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op.add("Constraints.Output1.DataInfo.Attributes.number","2");
		op.add("Constraints.Output1.DataInfo.Attributes.Atr1.type","ByteWritable");
		op.add("Constraints.Output1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op.add("Constraints.Output1.Engine.DB.NoSQL.HBase.key","Atr1");
		op.add("Constraints.Output1.Engine.DB.NoSQL.HBase.value","Atr2");
		op.add("Constraints.Output1.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		op.add("Constraints.OpSpecification.Algorithm.Join.JoinCondition","in1.atr1 = in2.atr2");
		op.add("Constraints.OpSpecification.Algorithm.Join.type", "HashJoin");

		//op.add("Properties.MaintainTags", ".*");
		
		System.out.println(op.toKeyValues("\n"));

		Operator op1 = new Operator("HBase_HashJoin", "/tmp");
		op1.add("Constraints.Input1.DataInfo.Attributes.number","1");
		op1.add("Constraints.Input1.DataInfo.Attributes.Atr1.type","ByteWritable");
		op1.add("Constraints.Input1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op1.add("Constraints.Input1.Engine.DB.NoSQL.HBase.key","Atr1");
		op1.add("Constraints.Input1.Engine.DB.NoSQL.HBase.value","Atr2");
		op1.add("Constraints.Input1.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op1.add("Constraints.Input2.DataInfo.Attributes.number","2");
		op1.add("Constraints.Input2.DataInfo.Attributes.Atr1.type","ByteWritable");
		op1.add("Constraints.Input2.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op1.add("Constraints.Input2.Engine.DB.NoSQL.HBase.key","Atr1");
		op1.add("Constraints.Input2.Engine.DB.NoSQL.HBase.value","Atr2");
		op1.add("Constraints.Input2.Engine.DB.NoSQL.HBase.location","127.0.0.1");

		op1.add("Constraints.Output1.DataInfo.Attributes.number","2");
		op1.add("Constraints.Output1.DataInfo.Attributes.Atr1.type","ByteWritable");
		op1.add("Constraints.Output1.DataInfo.Attributes.Atr2.type","List<ByteWritable>");
		op1.add("Constraints.Output1.Engine.DB.NoSQL.HBase.key","Atr1");
		op1.add("Constraints.Output1.Engine.DB.NoSQL.HBase.value","Atr2");
		op1.add("Constraints.Output1.Engine.DB.NoSQL.HBase.location","127.0.0.1");
		
		op1.add("Constraints.OpSpecification.Algorithm.Join.JoinCondition","in1.atr1 = in2.atr2");
		op1.add("Constraints.OpSpecification.Algorithm.Join.type", "MergeJoin");
		
		op1.writeToPropertiesFile("/home/nikos/test1");*/

		System.exit(0);
		AbstractOperator abstractOp = new AbstractOperator("JoinOp");
		abstractOp.add("Constraints.Input1.DataInfo.Attributes.number","1");
		abstractOp.add("Constraints.Input2.DataInfo.Attributes.number","2");
		abstractOp.add("Constraints.Output1.DataInfo.Attributes.number","2");
		abstractOp.addRegex(new NodeName("Constraints.OpSpecification.Algorithm.Join", new NodeName(".*", null, true), false), ".*");
		//abstractOp.add("Constraints.OpSpecification.Algorithm.Join.type","(HashJoin|MergeJoin)");

		System.out.println(abstractOp);
		long start = System.currentTimeMillis();
		/*for (int i = 0; i < 1000; i++) {
			System.out.println(abstractOp.checkMatch(op));
			System.out.println(abstractOp.checkMatch(op1));
		}*/
		System.out.println(abstractOp.checkMatch(op));
		System.out.println(abstractOp.checkMatch(op1));
		long stop = System.currentTimeMillis();
		System.out.println("Time (s): "+((double)(stop-start))/1000.0);
	}



	
}
