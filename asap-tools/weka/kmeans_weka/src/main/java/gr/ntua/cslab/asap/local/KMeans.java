package gr.ntua.cslab.asap.local;

import java.io.File;
import java.io.PrintStream;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

        
public class KMeans {
  private static final String USAGE = "use parameters: <my_filepath.csv> <K>";
  static int K = 1;
  static File inFile;
  private static PrintStream err=null;
  static int maxIteration = 1000;
  final static int maxK = 1000;
  

  static void readParams(String args[]) throws Exception{
      if(args.length!=3) throw new Exception("WRONG ARGUMENTS\n"+USAGE);
      K = Integer.parseInt(args[1]);
      if(K<0 || K>maxK) throw new Exception("Invalid K. should be positive, <"+maxK);
      String filePath = args[0];
      inFile = new File(filePath);
      if (!inFile.exists()) throw new Exception("Input file does not exist: "+filePath);
      System.out.println("K-means on file: "+filePath);
      System.out.println("For K: "+K);
      maxIteration=Integer.parseInt(args[2]);
      //hack to avoid some weka error  messages
      err= System.err;
      System.setErr(new PrintStream(new java.io.OutputStream() { public void write(int b) {}}));
  }
  
  
    public static void main(String args[]) throws Exception {
        //read the input params
        readParams(args);
        CSVLoader loader = new CSVLoader();
        loader.setSource(inFile);
        Instances data = loader.getDataSet();
        System.setErr(err);//hack to avoid some error messages

        // Create the KMeans object.
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(K);
        kmeans.setMaxIterations(maxIteration);
        kmeans.setPreserveInstancesOrder(true);

        // Perform K-Means clustering.
        try {  
            kmeans.buildClusterer(data);
        } catch (Exception ex) {
            System.err.println("Unable to buld Clusterer: " + ex.getMessage());
            ex.printStackTrace();
        }

        // print out the cluster centroids
        Instances centroids = kmeans.getClusterCentroids();
        for (int i = 0; i < K; i++) {
            System.out.print("Cluster " + i + " size: " + kmeans.getClusterSizes()[i]);
            System.out.println(" Centroid: " + centroids.instance(i));
        }

//        Print Assignments:
//        int[] assignments = kmeans.getAssignments();
//        System.out.println("Length: "+assignments.length);
//        for (int i = 0; i < assignments.length; i++) {
//            System.out.println(assignments[i]);
//            
//        }

    }
}