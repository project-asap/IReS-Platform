package gr.ntua.cslab.asap.utils;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;

import java.util.ArrayList;

/**
 * Created by victor on 10/14/15.
 */
public interface DataSource {
   ArrayList<OutputSpacePoint> getOutputSpacePoints();
}
