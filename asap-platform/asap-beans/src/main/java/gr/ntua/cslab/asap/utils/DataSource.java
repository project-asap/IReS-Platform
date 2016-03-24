package gr.ntua.cslab.asap.utils;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import java.util.ArrayList;

/**
 * Created by victor on 10/14/15.
 */
public interface DataSource {
   /**
    * --- getOutputSpacePoints ---
 * @param metric 
    * @return An ArrayList of output space points to be given as input to PANIC models for training
    */
   ArrayList<OutputSpacePoint> getOutputSpacePoints(String metric);

ArrayList<OutputSpacePoint> getOutputSpacePoints();
}
