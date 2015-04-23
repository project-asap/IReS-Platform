package gr.ntua.ece.cslab.panic.core.models;

import java.util.HashMap;

import weka.classifiers.meta.ClassificationViaRegression;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class CRegressionDeprecated extends AbstractWekaModel{

    public CRegressionDeprecated() {
        super();
        this.classifier = new ClassificationViaRegression();
    }

    @Override
    public void configureClassifier(HashMap<String, String> conf) {
        // nothing here
    }
    
    

    
}
