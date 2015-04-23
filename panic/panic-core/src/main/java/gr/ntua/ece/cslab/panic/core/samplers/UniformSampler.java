/*
 * Copyright 2014 Giannis Giannakopoulos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.panic.core.samplers;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;

/**
 * Creates a grid out of the input space and returns the grid points.
 * @author Giannis Giannakopoulos
 */
public class UniformSampler extends AbstractStaticSampler {

    private Integer index;
    private Double pivot;
    
    public UniformSampler() {
        super();
        this.index = -1;
        this.pivot = 0.0;
    }

    @Override
    public void configureSampler() {
        super.configureSampler();
        this.index = -1;
        this.pivot = 1.0 / this.samplingRate;
    }

    @Override
    public InputSpacePoint next() {
        super.next();
        this.index = (int)Math.floor((this.pointsPicked-1) * this.pivot+this.pivot/2.0);
        InputSpacePoint result = this.getPointById(this.index);
        return result;
    }
}
