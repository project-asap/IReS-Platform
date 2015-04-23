package gr.ntua.ece.cslab.panic.core.samplers;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class PointsDistance {
    
    /**
     * Returns the eucleidian distance between two input space points.
     * @param a
     * @param b
     * @return 
     */
    public static double eucleidian(InputSpacePoint a, InputSpacePoint b) {
        double sum =0.0;
        for(String s : a.getKeysAsCollection()) {
            sum += Math.pow(a.getValue(s)-b.getValue(s), 2);
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Returns the manhattan distance between two different points.
     * @param a
     * @param b
     * @return 
     */
    public static double mahnattan(InputSpacePoint a , InputSpacePoint b) {
        double sum = 0.0;
        for(String s : a.getKeysAsCollection()) {
            sum += Math.abs(a.getValue(s) - b.getValue(s));
        }
        return sum;
    }
    
}
