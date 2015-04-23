/*
 * Copyright 2014 Giannis Giannakopoulos.
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

package gr.ntua.ece.cslab.panic.core.models;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import java.util.HashMap;
import java.util.List;

import weka.classifiers.Classifier;

/**
 * This interface is inherited to each defined approximation model.
 * 
 * @author Giannis Giannakopoulos
 */

public interface Model {
    
    /**
     * Provide a new point with its value to the model. The model is retrained.
     * @param point the point sampled
     * @throws java.lang.Exception
     */
    public void feed(OutputSpacePoint point) throws Exception;
    
    /**
     * Provide a new point with its value to the model and determine whether 
     * the model will be retrained.
     * @param point the point sampled
     * @param retrain determine whether the model will be retrained
     * @throws java.lang.Exception
     */
    public void feed(OutputSpacePoint point, boolean retrain)  throws Exception;
    
    /**
     * Method used to train the model object. The model is built according 
     * to the point that were previously provided.
     * @throws java.lang.Exception
     */
    public void train() throws Exception;
    
    /**
     * This method returns the approximated value as estimated by the model.
     * @param point The input space point used
     * @return The output space point
     * @throws java.lang.Exception
     */
    public OutputSpacePoint getPoint(InputSpacePoint point) throws Exception;

    public OutputSpacePoint getPoint(InputSpacePoint point, OutputSpacePoint outputPoint) throws Exception;
    
    /**
     * Method used to provide specific instructions for each classifier.
     */
    public void configureClassifier(HashMap<String, String> conf);

    /**
     * Method used to get the sampled points along with their experimental values.
     * @return 
     */
    public List<OutputSpacePoint> getOriginalPointValues();

	public void serialize(String file) throws Exception; 
	
	public Classifier getClassifier();
	public void setClassifier(Classifier classifier);

	HashMap<String, String> getOutputSpace();

	void setOutputSpace(HashMap<String, String> outputSpace);

	void setInputSpace(HashMap<String, String> inputSpace);

	HashMap<String, String> getInputSpace();
}
