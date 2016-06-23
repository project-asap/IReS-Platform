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


package gr.ntua.cslab.asap.optimization;

import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ML {
    public static double totalError(Model model){
        List<OutputSpacePoint> actualPoints = model.getOriginalPointValues();
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : actualPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                totalError += Math.abs(point.getValue() - predicted);
            }
            catch(Exception e){ continue; }
        }
        totalError=totalError/actualPoints.size();
        return totalError;
    }
	
    public static double totalAbsError(Model model,
			ArrayList<OutputSpacePoint> testPoints) {
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : testPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                totalError += Math.abs(point.getValue() - predicted);
            }
            catch(Exception e){ continue; }
        }
        totalError=totalError/testPoints.size();
        return totalError;
	}
	
    public static double totalSquaredError(Model model,
			ArrayList<OutputSpacePoint> testPoints) {
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : testPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                totalError += Math.abs(point.getValue() - predicted)*Math.abs(point.getValue() - predicted);
            }
            catch(Exception e){ continue; }
        }
        totalError=totalError/testPoints.size();
        return totalError;
	}
    
	public static double totalRelError(Model model,
			ArrayList<OutputSpacePoint> testPoints) {
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : testPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                totalError += Math.abs(point.getValue() - predicted)/Math.abs(point.getValue());
            }
            catch(Exception e){ continue; }
        }
        totalError=totalError/testPoints.size();
        return totalError;
	}
	

	public static double medianRelError(Model model,
			ArrayList<OutputSpacePoint> testPoints) {
		List<Double> errors = new ArrayList();
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : testPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                errors.add(Math.abs(point.getValue() - predicted)/Math.abs(point.getValue()));
            }
            catch(Exception e){ continue; }
        }
        Collections.sort(errors);
        return errors.get(errors.size()/2);
	}
	
	public static double medianAbsError(Model model,
			ArrayList<OutputSpacePoint> testPoints) {
		List<Double> errors = new ArrayList();
        double totalError = 0.0;
        double predicted;
        for (OutputSpacePoint point : testPoints){
            try{
                predicted = model.getPoint(point.getInputSpacePoint()).getValue();
                errors.add(Math.abs(point.getValue() - predicted));
            }
            catch(Exception e){ continue; }
        }
        Collections.sort(errors);
        return errors.get(errors.size()/2);
	}
}
