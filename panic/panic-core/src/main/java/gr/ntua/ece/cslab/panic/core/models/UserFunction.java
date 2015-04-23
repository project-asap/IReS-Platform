package gr.ntua.ece.cslab.panic.core.models;

import java.util.HashMap;

import gr.ntua.ece.cslab.panic.core.classifier.UserFunctionClassifier;

public class UserFunction extends AbstractWekaModel {
    
    public UserFunction() {
        super();
        classifier = new UserFunctionClassifier();
    }
    
    @Override
    public void configureClassifier(HashMap<String, String> conf) {
    	
    	UserFunctionClassifier temp = (UserFunctionClassifier) classifier;
    	temp.setModel(this);
    	temp.configure(conf);
    }
    
    
}
