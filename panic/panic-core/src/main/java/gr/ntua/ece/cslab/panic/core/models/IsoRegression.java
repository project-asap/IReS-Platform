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

package gr.ntua.ece.cslab.panic.core.models;

import java.util.HashMap;

import weka.classifiers.functions.IsotonicRegression;

/**
 * Isotonic Regression, as implemented by WEKA.
 * @author Giannis Giannakopoulos
 */
public class IsoRegression extends AbstractWekaModel{

    public IsoRegression() {
        super();
        classifier = new IsotonicRegression();
    }

    @Override
    public void configureClassifier(HashMap<String, String> conf) {
        // no configuration for now
    }
}
