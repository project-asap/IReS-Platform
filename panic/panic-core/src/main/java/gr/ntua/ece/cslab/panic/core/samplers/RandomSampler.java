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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * This class implements random sampling. The multidimensional points are mapped
 * to one dimension, and then picked in a random way.
 * @author Giannis Giannakopoulos
 */
public class RandomSampler extends AbstractStaticSampler {

    private final Random random;
    private final Set<Integer> chosenIds;
    
    public RandomSampler() {
        super();
        this.random = new Random();
        this.chosenIds = new HashSet<Integer>();
    }

    @Override
    public InputSpacePoint next() {
        super.next();
        Integer randomId = this.random.nextInt(this.maxChoices);
        while(this.chosenIds.contains(randomId))
            randomId = this.random.nextInt(this.maxChoices);
        InputSpacePoint point  = this.getPointById(randomId);
        this.chosenIds.add(randomId);
        return point;
    }
}
