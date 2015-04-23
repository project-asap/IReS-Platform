/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.ntua.ece.cslab.panic.core.metrics;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class Metrics {
    
    private List<OutputSpacePoint> objective;
    private Model m;
    private List<InputSpacePoint> sampled;

    public Metrics() {
    }

    public Metrics(List<OutputSpacePoint> objective, Model m, List<InputSpacePoint> sampled) {
        this.objective = objective;
        this.m = m;
        this.sampled = sampled;
    }
    
    public double getMSE(){
        double sum = 0.0;
        for(OutputSpacePoint p : objective) {
            try {
                sum+=Math.pow(p.getValue()-this.m.getPoint(p.getInputSpacePoint()).getValue(),2);
            } catch (Exception ex) {
                Logger.getLogger(Metrics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sum/objective.size();
    }
    
    public double getAverageError() {
        double sum = 0.0;
        for(OutputSpacePoint p : objective) {
            try {
                sum+=Math.abs(p.getValue()-this.m.getPoint(p.getInputSpacePoint()).getValue());
            } catch (Exception ex) {
                Logger.getLogger(Metrics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sum/objective.size();
    }
    
    public double getDeviation() {
        return Math.sqrt(this.getMSE());
    }
    
    /**
     * Returns the coefficient of determination
     * @return 
     */
    public double getR() {
        double mean = this.findMeanForObserved();
        

        try {
            return 1-(this.findSSRes(mean)/this.findSSTot(mean));
//        double sum= 0.0;
//        for(OutputSpacePoint p : this.objective) {
//            sum+=p.getValue();
//        }
//        double mean = sum/this.objective.size();
//        double ssTot = 0.0;
//        double ssRes = 0.0;
//        for(OutputSpacePoint p :this.objective){
//            try {
//                ssTot += Math.pow(p.getValue()-mean, 2);
//                ssRes += Math.pow(this.m.getPoint(p.getInputSpacePoint()).getValue()-p.getValue(), 2);
//            } catch (Exception ex) {
//                Logger.getLogger(Metrics.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return 1-(ssRes/ssTot);
        } catch (Exception ex) {
            Logger.getLogger(Metrics.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }
    
    private double findMeanForObserved() {
        double sum=0.0;
        int count=0;
        for(OutputSpacePoint p : this.objective) {
            for (InputSpacePoint in : this.sampled) {
                if(p.getInputSpacePoint().equals(in)){
                    sum+=p.getValue();
                    count+=1;
                }
            }
        }
        return sum/count;
    }
    
    private double findSSTot(double mean) {
        double sum=0.0;
        for(OutputSpacePoint p : this.objective) {
            for (InputSpacePoint in : this.sampled) {
                if(p.getInputSpacePoint().equals(in)){
                    sum+=Math.pow(p.getValue()-mean,2);
                }
            }
        }
        return sum;
    }
    
    private double findSSReg(double mean) throws Exception {
        double sum=0.0;
        for(OutputSpacePoint p : this.objective) {
            for (InputSpacePoint in : this.sampled) {
                if(p.getInputSpacePoint().equals(in)){
                    sum+=Math.pow(this.m.getPoint(in).getValue()-mean,2);
                }
            }
        }
        return sum;
    }
    
    private double findSSRes(double mean) throws Exception {
        double sum=0.0;
        for(OutputSpacePoint p : this.objective) {
            for (InputSpacePoint in : this.sampled) {
                if(p.getInputSpacePoint().equals(in)){
                    sum+=Math.pow(this.m.getPoint(in).getValue()-p.getValue(),2);
                }
            }
        }
        return sum;
    }
}
