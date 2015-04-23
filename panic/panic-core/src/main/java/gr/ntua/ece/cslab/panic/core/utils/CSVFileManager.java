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

package gr.ntua.ece.cslab.panic.core.utils;

import au.com.bytecode.opencsv.CSVReader;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class CSVFileManager {

    private String filename;
    private int numberOfInputDimensions;
    private int outputDimensionIndex;
    private char delimiter = ',';
    private List<String> variables;
    
    public CSVFileManager() {
        
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String buffer = null;
            if(reader.ready()){
                buffer = reader.readLine();
                String[] header = buffer.split(delimiter+"");
                
                numberOfInputDimensions  = header.length -1;
                outputDimensionIndex  = header.length -1;
                variables = new ArrayList<String>(header.length);
                for (int i = 0; i < header.length; i++) {
					variables.add(header[i]);
				}
                //System.out.println(variables);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CSVFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Default number equals to number of column to CSV -1.
     * @return 
     */
    public int getNumberOfInputDimensions() {
        return numberOfInputDimensions;
    }

    public void setNumberOfInputDimensions(int numberOfInputDimensions) {
        this.numberOfInputDimensions = numberOfInputDimensions;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Default number equals to number of columns to CSV -1.
     * @return 
     */
    public int getOutputDimensionIndex() {
        return outputDimensionIndex;
    }

    public void setOutputDimensionIndex(int outputDimensionIndex) {
        this.outputDimensionIndex = outputDimensionIndex;
    }    
    
    public List<OutputSpacePoint> getOutputSpacePoints() {
        List<OutputSpacePoint> results = null;
        try {
            CSVReader reader = new CSVReader(new FileReader(filename), delimiter, '#', 1);
            String[] line;
            results = new LinkedList<OutputSpacePoint>();
            while((line = reader.readNext())!=null) {
                OutputSpacePoint point = new OutputSpacePoint();
                point.setInputSpacePoint(new InputSpacePoint());
                for(int i=0;i<numberOfInputDimensions;i++)
                    point.getInputSpacePoint().addDimension(variables.get(i), new Double(line[i]));
                HashMap<String, Double> v = new HashMap<String, Double>();
                v.put(variables.get(outputDimensionIndex), new Double(line[outputDimensionIndex]));
				point.setValues(v);
                results.add(point);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return results;
    }
    
    public List<InputSpacePoint> getInputSpacePoints() {
        List<OutputSpacePoint> points = this.getOutputSpacePoints();
        List<InputSpacePoint> results = new LinkedList<InputSpacePoint>();
        for(OutputSpacePoint p :points)
            results.add(p.getInputSpacePoint());
        return results;
    }
    
    /**
     * Method returning the district values of each dimension. Useful for samplers.
     * @return
     */
  
    public HashMap<String, List<Double>> getDimensionRanges() {
        HashMap<String, Set<Double>> temp = new HashMap<String, Set<Double>>();
        List<InputSpacePoint> points = this.getInputSpacePoints();
        for(InputSpacePoint p : points) {
            for(String key : p.getKeysAsCollection()) {
                if(temp.get(key) == null)
                    temp.put(key, new HashSet<Double>());
                temp.get(key).add(p.getValue(key));
            }
        }
        HashMap<String, List<Double>> results = new HashMap<String, List<Double>>();
        for(String s: temp.keySet())
            results.put(s, new LinkedList<Double>(temp.get(s)));
        return results;
    }
    
    public OutputSpacePoint getActualValue(InputSpacePoint point) {
        for(OutputSpacePoint p : this.getOutputSpacePoints())
            if(p.getInputSpacePoint().equals(point))
                return p;
        return null;
    }
//    public static void main(String[] args) {
//        CSVFileManager loader = new CSVFileManager();
//        loader.setFilename(args[0]);
//        loader.setNumberOfInputDimensions(1);
//        loader.setOutputDimensionIndex(1);
//        System.out.println(loader.getOutputSpacePoints());
//    }
}
