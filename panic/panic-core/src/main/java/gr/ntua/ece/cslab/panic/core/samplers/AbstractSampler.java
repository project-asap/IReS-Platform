/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ntua.ece.cslab.panic.core.samplers;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class AbstractSampler implements Sampler {

    protected HashMap<String, List<Double>> ranges;
    protected Double samplingRate;
    protected int pointsPicked;
    protected int maxChoices;

    public AbstractSampler() {
        this.ranges = new HashMap<String, List<Double>>();
        this.samplingRate = Double.MAX_VALUE;
        this.pointsPicked = 0;

    }

    @Override
    public void setDimensionsWithRanges(HashMap<String, List<Double>> ranges) {
        this.ranges = ranges;
        for (String s : this.ranges.keySet()) {      // sort each dimension ranges
            Collections.sort(this.ranges.get(s));
        }
    }

    @Override
    public void setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
    }

    @Override
    public boolean hasMore() {
        return this.pointsPicked < (int) Math.floor(this.maxChoices * this.samplingRate);
    }

    @Override
    public InputSpacePoint next() {
        this.pointsPicked++;
        return null;
    }

    @Override
    public void configureSampler() {
        this.maxChoices = 1;
        for (String s : this.ranges.keySet()) {
            this.maxChoices *= this.ranges.get(s).size();
        }
    }

    /**
     * Method which maps every multidimensional point to a single dimension.
     * This dimension is referenced by an Integer id. The objective of the
     * function is to convert ids to InputSpacePoints.
     *
     * @param id
     * @return
     */
    protected InputSpacePoint getPointById(int id) {
        int identifier = id;
        InputSpacePoint point = new InputSpacePoint();
        for (String s : this.ranges.keySet()) {
            int index = identifier % this.ranges.get(s).size();
            identifier /= this.ranges.get(s).size();
            point.addDimension(s, this.ranges.get(s).get(index));
        }
        return point;
    }

}
