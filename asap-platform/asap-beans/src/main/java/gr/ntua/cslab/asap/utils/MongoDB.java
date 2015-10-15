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
public class MongoDB extends DataSource{
    MongoClient client;
    MongoDatabase mdb;
    String db;
    String host;

    /** --- Constructor ---
     * @param host The MongoDB host
     * @param db The database
     * */
    public MongoDB(String host, String db){
        this.client = new MongoClient(host);
        this.db = db;
        this.host = host;
        mdb = client.getDatabase(db);
    }

    /**
     * --- getOutputSpacePoints ---
     * @param collection The collection name (usually the operator's name, e.g. spark_kmeans)
     * @param inputSpace The list of the input space attribute names, defined in the description file(e.g. documents)
     * @param outputSpace The list of the output space attribute names, defined in the description file(e.g. execTime)
     * @return An ArrayList of output space points to be given as input to PANIC models for training
     */
    public ArrayList<OutputSpacePoint> getOutputSpacePoints(String collection, List<String> inputSpace,
                                                  List<String> outputSpace){
        InputSpacePoint isp = new InputSpacePoint();
        HashMap<String, Double> hm;
        ArrayList<OutputSpacePoint> results = new ArrayList<OutputSpacePoint>();
        MongoCollection mc = mdb.getCollection(collection);
        Document projection = new Document();
        OutputSpacePoint osp;

        /* Selection/Projection Query Construction */
        for (String is : inputSpace)
            projection.append(is, "true");
        for (String os : outputSpace)
            projection.append(os, "true");

        FindIterable obj = mc.find().projection(projection);

        try {
            for (Object item : obj) {
                Document doc = (Document) item;
                isp = new InputSpacePoint();
                hm = new HashMap<String, Double>();

                for (String is : inputSpace) {
                    String key = is;
                    Object value = doc.get(key);
                    double val;

                    if (value != null) {
                        if (value.getClass() == Integer.class)
                            val = ((Integer) value) * 1.0;
                        else
                            val = (Double) value;
                        isp.addDimension(key, val);
                    }
                }

                for (String os : outputSpace) {
                    hm.put(os, doc.getDouble(os));
                }

                osp = new OutputSpacePoint();
                osp.setInputSpacePoint(isp);
                osp.setValues(hm);
                results.add(osp);

            }
        }
        catch(Exception e){
            System.out.println("MONGO EXCEPTION: "+e);
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
        MongoDB mdb = new MongoDB("asapmaster","metrics");
        ArrayList<String> in = new ArrayList<String>();
        ArrayList<String> out = new ArrayList<String>();

        in.add("dimensions");
        in.add("documents");
        in.add("output_size");
        out.add("time");

        for (OutputSpacePoint o : mdb.getOutputSpacePoints("spark2mahout", in, out)){
            System.out.println(o);
        }
    }

}
