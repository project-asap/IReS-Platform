/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.ntua.ece.cslab.panic.core.models;

import java.util.HashMap;

import weka.classifiers.functions.RBFNetwork;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class RBF extends AbstractWekaModel{

    public RBF() {
        super();
        this.classifier = new RBFNetwork();
    }

    @Override
    public void configureClassifier(HashMap<String, String> conf) {
        //no conf
    }
    
    
    
}
