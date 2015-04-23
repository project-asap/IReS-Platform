package gr.ntua.ece.cslab.panic.core.samplers;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Discretization;
import gr.ntua.ece.cslab.panic.core.models.IsoRegression;
import gr.ntua.ece.cslab.panic.core.models.Model;
import gr.ntua.ece.cslab.panic.core.utils.CSVFileManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * GreedyDimensionSampler is a greedy algorithm executing adaptive sampling.
 * <br/>
 * The algorithm works in two phases:
 * <br/>
 * <ol>
 * <li> returns all the points where each dimension takes its lowest or maximum
 * values.</li>
 * <li>For each dimension, the median of the two points with the max difference
 * is picked and a new point is formed for all the dimensions.</li>
 * </ol>
 *
 * In the second case, if the specified point exists a random point is returned.
 *
 * <br/>
 * The values used for feeding the sampler are the values from the deployment,
 * and not the values as estimated from the model itself.
 *
 * @author Giannis Giannakopoulos
 */
public class GreedyDimensionSampler extends AbstractAdaptiveSampler {

    private Set<InputSpacePoint> picked;
    private RandomSampler randomSampler;
    
    public GreedyDimensionSampler() {
        super();
        this.picked = new HashSet<InputSpacePoint>();
    }

    @Override
    public void configureSampler() {
        super.configureSampler();
        randomSampler = new RandomSampler();
        randomSampler.setDimensionsWithRanges(this.ranges);
        randomSampler.setSamplingRate(this.samplingRate);
        randomSampler.configureSampler();
    }

    
    @Override
    public InputSpacePoint next() {
        super.next();
        InputSpacePoint sample;
        if (this.pointsPicked <= Math.pow(2, this.ranges.size())) {
            sample = this.getBorderPoint();
        } else {
            sample = this.getNextPoint();
        }
        this.picked.add(sample);
        return sample;
    }

    // method used to estimate the next border point
    private InputSpacePoint getBorderPoint() {
        int bitsPadding = this.ranges.size();
        int pointIdex = this.pointsPicked - 1; // super.next() is already called
        Integer unpadded = new Integer(Integer.toBinaryString(pointIdex));
        char[] bitmask = String.format("%0" + bitsPadding + "d", unpadded).toCharArray();
        List<String> keys = new LinkedList<String>(this.ranges.keySet());
        Collections.sort(keys);

        InputSpacePoint result = new InputSpacePoint();
        int index = 0;
        for (String key : keys) {
            if (bitmask[index++] == '0') {
                result.addDimension(key, this.ranges.get(key).get(0));
            } else {
                result.addDimension(key, this.ranges.get(key).get(this.ranges.get(key).size() - 1));
            }
        }
        return result;
    }

    // method used in the second step of the algorithm, to fetch 
    private InputSpacePoint getNextPoint() {
        double maxDifference = 0.0;
        OutputSpacePoint a = null, b = null;
        for(OutputSpacePoint x1 : this.model.getOriginalPointValues()) {
            for(OutputSpacePoint x2 : this.model.getOriginalPointValues()) {
                InputSpacePoint median = this.getMedianPoint(x1.getInputSpacePoint(), x2.getInputSpacePoint());
                if(!this.picked.contains(median)) {
                    double current = Math.abs(x1.getValue() - x2.getValue());
                    if(current > maxDifference) {
                        a = x1;
                        b = x2;
                        maxDifference  = current;
                    }
                }
            }
        }
        if(a==null || b==null)
            return this.getRandomPoint();
        return this.getMedianPoint(a.getInputSpacePoint(), b.getInputSpacePoint());
    }
    
    private InputSpacePoint getRandomPoint() {
        InputSpacePoint next = randomSampler.next();
        while(this.picked.contains(next))
            next = randomSampler.next();
        return next;
    }
    
    
    private InputSpacePoint getMedianPoint(InputSpacePoint a, InputSpacePoint b) {
        InputSpacePoint result = new InputSpacePoint();
        for(String s : a.getKeysAsCollection()) {
            result.addDimension(s, this.getClosestAllowedValue(s, (a.getValue(s)+b.getValue(s)/2.0)));
        }
        return result;
    }
    
    private double getClosestAllowedValue(String key, double value) {
        double candidate = this.ranges.get(key).get(0);
        for (double d : this.ranges.get(key)) {
            if (Math.abs(d - value) < Math.abs(candidate - value)) {
                candidate = d;
            }
        }
        return candidate;
    }
   
     
    public static void main(String[] args) throws Exception {
        CSVFileManager file = new CSVFileManager();
        file.setFilename(args[0]);

        Model model = new IsoRegression();
        model.configureClassifier(new HashMap<String, String>());

        GreedyDimensionSampler sampler = new GreedyDimensionSampler();
        sampler.setSamplingRate(0.3);
        sampler.setDimensionsWithRanges(file.getDimensionRanges());
        sampler.configureSampler();
        sampler.setModel(model);

        while (sampler.hasMore()) {
            InputSpacePoint sampledPoint = sampler.next();
            if (sampledPoint == null) {
                System.out.println("Breaking the habit... Why null?");
                break;
            }
            OutputSpacePoint outPoint = file.getActualValue(sampledPoint);
            if (outPoint != null) {
                System.out.println("Outpoint " + outPoint);
            }
            model.feed(outPoint, false);
        }

        model.train();
        for (InputSpacePoint p : file.getInputSpacePoints()) {
            System.out.println(model.getPoint(p));
        }
    }
}