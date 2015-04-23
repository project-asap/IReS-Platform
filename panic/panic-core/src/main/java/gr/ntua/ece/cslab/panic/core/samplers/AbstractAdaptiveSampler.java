package gr.ntua.ece.cslab.panic.core.samplers;

import gr.ntua.ece.cslab.panic.core.models.Model;

/**
 * This class implements various method from the Sampler interface, used as a base class
 * for the implementation of various adaptive sampling methods.
 * @author Giannis Giannakopoulos
 */
public abstract class AbstractAdaptiveSampler extends AbstractSampler{

    /**
     * This model guides the sampler to pick better points.
     */
    protected Model model;
    
    
    /**
     * Default constructor.
     */
    public AbstractAdaptiveSampler() {
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
