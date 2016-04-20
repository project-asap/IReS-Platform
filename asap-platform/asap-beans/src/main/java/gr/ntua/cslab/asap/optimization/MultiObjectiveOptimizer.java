/*
 * Copyright 2016 ASAP.
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


package gr.ntua.cslab.asap.optimization;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;
import org.apache.log4j.Logger;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiObjectiveOptimizer extends AbstractProblem {
    protected static Model model;
    protected static InputSpacePoint isp;
    protected static List<RealVariable> variables;
    protected static List<String> missingVars = new ArrayList<>();
    private static Logger logger = Logger.getLogger(MultiObjectiveOptimizer.class);
    protected static HashMap<String, List<Model>> models;
    protected static String policy;

    public MultiObjectiveOptimizer() {
        super(variables.size(), 1);
    }

    @Override
    public void evaluate(Solution solution) {
        try {
            double[] x = EncodingUtils.getReal(solution);
            double f; //Objective 1 (exec time)
            for (int i=0; i<solution.getNumberOfVariables(); ++i){
                isp.addDimension(missingVars.get(i), x[i]);
            }

            HashMap<String, Double> mValues = new HashMap<>();
            for (Map.Entry<String, List<Model>> m: models.entrySet()){
                mValues.put(m.getKey(), m.getValue().get(0).getPoint(isp).getValue());
            }

            f = OptimizeMissingMetrics.computePolicyFunction(mValues, policy);
            solution.setObjective(0, f);
        }
        catch (Exception e){
            logger.info("MultiObjectiveOptimizer Exception: "+e.getLocalizedMessage());
        }
    }

    @Override
    public Solution newSolution() {
        Solution sol = new Solution(this.getNumberOfVariables(), this.getNumberOfObjectives());

        for (int i=0; i<variables.size(); ++i) {
            RealVariable rv = variables.get(i);
            // Set the variables to be optimized (missing variables)
            Double min = rv.getLowerBound();
            Double max = rv.getUpperBound();
            sol.setVariable(i, new RealVariable(min, max));
        }

        return sol;
    }
}
