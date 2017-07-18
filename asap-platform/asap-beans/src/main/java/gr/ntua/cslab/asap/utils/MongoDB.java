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


package gr.ntua.cslab.asap.utils;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import org.apache.log4j.Logger;
import org.bson.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * --- MongoDB Class ---
 * Establish a connection with a MongoDB database.
 */
public class MongoDB implements DataSource{
	private static Logger logger = Logger.getLogger(MongoDB.class.getName());
    MongoClient client;
    MongoDatabase mdb;
    String db;
    String host;
    String collection;
    List<String> inputSpace;
    List<String> outputSpace;

    /** --- Constructor ---
     * @param host The MongoDB host
     * @param db The database
     * @param collection The collection name (usually the operator's name, e.g. spark_kmeans)
     * @param inputSpace The list of the input space attribute names, defined in the description file(e.g. documents)
     * @param outputSpace The list of the output space attribute names, defined in the description file(e.g. execTime)
     * */
    public MongoDB(String host, String db, String collection, List<String> inputSpace,
                   List<String> outputSpace){
        this.client = new MongoClient(host);
        this.db = db;
        this.host = host;
        this.collection = collection;
        this.inputSpace = inputSpace;
        this.outputSpace = outputSpace;
        mdb = client.getDatabase(db);
    }

    /**
     * --- getOutputSpacePoints ---
     * @return An ArrayList of output space points to be given as input to PANIC models for training
     */
    public ArrayList<OutputSpacePoint> getOutputSpacePoints(String metric){
        InputSpacePoint isp = new InputSpacePoint();
        InputSpacePoint isp1 = new InputSpacePoint();
        HashMap<String, Double> hm;
        ArrayList<OutputSpacePoint> results = new ArrayList<OutputSpacePoint>();
        MongoCollection mc = mdb.getCollection(collection);
        Document projection = new Document();
        OutputSpacePoint osp;

        /* Selection/Projection Query Construction */
        for (String is : inputSpace){
			String in1= is.replace('.', '@');
            projection.append(in1, "true");
        }

    	if(metric.equals("execTime")){
            projection.append("time", "true");
    	}
    	else{
            String metric1= metric.replace('.', '@');
            logger.info("This is mongo! I am building the projection for metric: " + metric);
            projection.append(metric1, "true");
    	}
        logger.info("The projection is: " + projection);

        FindIterable obj = mc.find().projection(projection);

        try {
        	if(obj==null)
                return null;
        		
            for (Object item : obj) {
                Document doc = (Document) item;
                isp = new InputSpacePoint();
                isp1 = new InputSpacePoint();
                hm = new HashMap<String, Double>();

                for (String is : inputSpace) {
                    String key = is.replace('.', '@');
                    Object value = doc.get(key);
                    double val;

                    if (value != null) {
                        if (value.getClass() == Integer.class)
                        	val = (Integer) value * 1.0;
                        else
                            val = (Double) value;
                        isp.addDimension(key, val);
                        isp1.addDimension(is, val);
                    }
                }

                osp = new OutputSpacePoint();
                osp.setInputSpacePoint(isp1);
                String key = metric.replace('.', '@');
                if(key.equals("execTime")){
                	key="time";
            	}
		          Object value = doc.get(key);
		          double val=0;
		
		          if (value != null) {
		              if (value.getClass() == Integer.class)
		                  val = (Integer) value * 1.0;
		              else
		                  val = (Double) value;
		          }
		          
		          //double value = doc.getDouble(key);
		          osp.setValue(val);
		          hm.put(metric, val);
		          
//                for (String os : outputSpace) {
//                    String key = os.replace('.', '@');
//                    Object value = doc.get(key);
//                    double val=0;
//
//                    if (value != null) {
//                        if (value.getClass() == Integer.class)
//                            val = (Integer) value * 1.0;
//                        else
//                            val = (Double) value;
//                    }
//                    
//                    //double value = doc.getDouble(key);
//                    osp.setValue(val);
//                    hm.put(os, val);
//                }

                osp.setValues(hm);
                results.add(osp);

            }
        }
        catch(Exception e){
        	logger.info("MONGO EXCEPTION: "+e.getMessage());
        	e.printStackTrace();
        }
        System.out.println(results);
        return results;
    }

    /**
     * --- getOutputSpacePoints ---
     * @return An ArrayList of output space points to be given as input to PANIC models for training
     */
    public ArrayList<OutputSpacePoint> getOutputSpacePoints(){
        InputSpacePoint isp = new InputSpacePoint();
        InputSpacePoint isp1 = new InputSpacePoint();
        HashMap<String, Double> hm;
        ArrayList<OutputSpacePoint> results = new ArrayList<OutputSpacePoint>();
        MongoCollection mc = mdb.getCollection(collection);
        Document projection = new Document();
        OutputSpacePoint osp;

        /* Selection/Projection Query Construction */
        for (String is : inputSpace){
			String in1= is.replace('.', '@');
            projection.append(in1, "true");
        }
        for (String os : outputSpace){
        	if(os.equals("execTime")){
	            projection.append("time", "true");
        	}
        	else{
				String os1= os.replace('.', '@');
	            projection.append(os1, "true");
        	}
        }
        
        logger.info(projection);
        FindIterable obj = mc.find().projection(projection);

        try {
        	if(obj==null)
                return null;
        		
            for (Object item : obj) {
                Document doc = (Document) item;
                isp = new InputSpacePoint();
                isp1 = new InputSpacePoint();
                hm = new HashMap<String, Double>();

                for (String is : inputSpace) {
                    String key = is.replace('.', '@');
                    Object value = doc.get(key);
                    double val;

                    if (value != null) {
                        if (value.getClass() == Integer.class)
                        	val = (Integer) value * 1.0;
                        else
                            val = (Double) value;
                        isp.addDimension(key, val);
                        isp1.addDimension(is, val);
                    }
                }

                osp = new OutputSpacePoint();
                osp.setInputSpacePoint(isp1);
                for (String os : outputSpace) {
                    String key = os.replace('.', '@');
                    if(key.equals("execTime")){
                    	key="time";
                	}
                    Object value = doc.get(key);
                    double val=0;

                    if (value != null) {
                        if (value.getClass() == Integer.class)
                            val = (Integer) value * 1.0;
                        else
                            val = (Double) value;
                    }
                    
                    //double value = doc.getDouble(key);
                    osp.setValue(val);
                    hm.put(os, val);
                }

                osp.setValues(hm);
                results.add(osp);

            }
        }
        catch(Exception e){
        	logger.info("MONGO EXCEPTION: "+e.getMessage());
        	e.printStackTrace();
        }

        return results;
    }
    
    @Override
    public String toString(){
        return "--- MongoDB Connection ---\n"+
                "Host: "+this.host+"\n"+
                "DB: "+this.db+"\n------";
    }

    /**
     * Developer_Test_Method
     */
    public static void main(String[] args) {

        ArrayList<String> in = new ArrayList<String>();
        ArrayList<String> out = new ArrayList<String>();

        in.add("dimensions");
        in.add("documents");
        in.add("output_size");
        out.add("time");
        MongoDB mdb = new MongoDB("asapmaster","metrics", "spark2mahout",in, out);

        for (OutputSpacePoint o : mdb.getOutputSpacePoints("time")){
            System.out.println(o);
        }
    }

}
