package gr.ntua.cslab.asap.optimization;

import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;

import java.util.List;

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
}
