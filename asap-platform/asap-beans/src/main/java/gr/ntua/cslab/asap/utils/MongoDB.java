package gr.ntua.cslab.asap.utils;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import org.bson.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * --- MongoDB Class ---
 * Establish a connection with a MongoDB database.
 */
public class MongoDB implements DataSource{
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
//        for (String os : outputSpace){
//			String os1= os.replace('.', '@');
//            projection.append(os1, "true");
//        }
        String metric1= metric.replace('.', '@');
        projection.append(metric1, "true");
        
        System.out.println(projection);
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
            System.out.println("MONGO EXCEPTION: ");
        	e.printStackTrace();
        }

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
			String os1= os.replace('.', '@');
            projection.append(os1, "true");
        }
        
        System.out.println(projection);
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
            System.out.println("MONGO EXCEPTION: ");
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
