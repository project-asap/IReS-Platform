/*
 * Copyright 2014 Gianis Giannakopoulos.
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

package gr.ntua.ece.cslab.panic.core.containers.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * This class contains profiling specific details and is used 
 * @author Giannis Giannakopoulos
 */
public class ProfilingInfo implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private int maxDeployments;
    private double desiredAccuracy;
    private List<String> models;
    private String sampler;
    private HashMap<String, List<Integer>> inputSpaceValues;

    public ProfilingInfo() {
    
    }
    
    public int getMaxDeployments() {
        return maxDeployments;
    }

    public void setMaxDeployments(int maxDeployments) {
        this.maxDeployments = maxDeployments;
    }

    public double getDesiredAccuracy() {
        return desiredAccuracy;
    }

    public void setDesiredAccuracy(double desiredAccuracy) {
        this.desiredAccuracy = desiredAccuracy;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public String getSampler() {
        return sampler;
    }

    public void setSampler(String sampler) {
        this.sampler = sampler;
    }

    public HashMap<String, List<Integer>> getInputSpaceValues() {
        return inputSpaceValues;
    }

    public void setInputSpaceValues(HashMap<String, List<Integer>> inputSpaceValues) {
        this.inputSpaceValues = inputSpaceValues;
    }
    
}
