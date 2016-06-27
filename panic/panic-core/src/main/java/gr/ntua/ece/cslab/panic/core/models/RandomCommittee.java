package gr.ntua.ece.cslab.panic.core.models;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RandomCommittee is the implementation of an EoC algorithm. The underneath 
 * classifier is a MultiLayerPerceptron.
 * @author Giannis Giannakopoulos
 */
public class RandomCommittee extends AbstractWekaModel{
	
    public RandomCommittee() {
        super();
        this.classifier = new weka.classifiers.meta.RandomCommittee();
    }
    @Override
    public void configureClassifier(HashMap<String, String> conf) {
        String[] options = this.classifier.getOptions();
        
        int index=0;
        for(String s:options) {
            if(s.equals("-W"))
                break;
            index++;
        }
        
        String[] newOptions = new String[index+2];
        System.arraycopy(options, 0, newOptions, 0, newOptions.length);
        newOptions[index+1] = "weka.classifiers.functions.MultilayerPerceptron";
        try {
            this.classifier.setOptions(newOptions);
        } catch (Exception ex) {
            Logger.getLogger(RandomCommittee.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}