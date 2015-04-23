package gr.ntua.ece.cslab.panic.core.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.jeval.Evaluator;
import gr.ntua.ece.cslab.panic.core.models.Model;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class UserFunctionClassifier extends Classifier{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2089701626134979114L;
	private Model model;
	protected HashMap<String, String> functions;
	
	public UserFunctionClassifier() {
		super();
		functions = new HashMap<String, String>();
	}

	@Override
	public void buildClassifier(Instances arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void setModel(Model model) {
		this.model=model;
	}

	public void configure(HashMap<String, String> conf) {
		//System.out.println(conf);
    	for( String outVar : model.getOutputSpace().keySet()){
        	functions.put(outVar, conf.get(outVar));
    	}
		//System.out.println(functions);
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		//System.out.println(instance);
		Evaluator evaluator = new Evaluator();
		Double res=0.0;
		int index = model.getInputSpace().size();
    	for( String outVar : model.getOutputSpace().keySet()){
    		String tempFunction = new String(functions.get(outVar));;
    		int inputIndex=0;
    		for(String inVar: model.getInputSpace().keySet()){
    			//System.out.println(inVar+" "+instance.value(inputIndex));
    			tempFunction=tempFunction.replace(inVar, instance.value(inputIndex)+"");
    			inputIndex++;
    		}
    		res = Double.parseDouble(evaluator.evaluate(tempFunction));
    		//System.out.println("adding: "+outVar + " " +tempFunction+" "+res);
    		instance.setValue(new Attribute(outVar,index++), res);
    	}
		//System.out.println(instance);
		//System.out.println(res);
		return res;
	}
	

}
