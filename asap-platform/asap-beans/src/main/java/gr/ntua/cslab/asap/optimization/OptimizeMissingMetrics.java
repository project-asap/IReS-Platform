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

import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.operators.SpecTree;
import gr.ntua.cslab.asap.operators.SpecTreeNode;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.MultiPoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Workflow missing metrics multi-objective optimization
 */

public class OptimizeMissingMetrics {
	private static final Log logger = LogFactory.getLog( OptimizeMissingMetrics.class);

	public static OutputSpacePoint findOptimalPointCheckAllSamples(
			HashMap<String, List<Model>> models, InputSpacePoint in,
			String policy, SpecTree optree, Operator operator) throws Exception {

		List<RealVariable> rvs = new ArrayList<>();
		MultiObjectiveOptimizer.missingVars.clear();
		HashMap<String, RealVariable> missing = new HashMap<>();

        /*
        *  Find all missing values from an operator configuration
        *  Configuration: Optimization.inputSpace.someVar
        * */
		for(Entry<String, Double> e : in.getValues().entrySet()){
			if(e.getValue()==null){
				String[] miss = optree.getNode("Optimization.inputSpace."+e.getKey()).toString()
						.replaceAll("\\(", "")
						.replaceAll("\\)", "")
						.split(",");

				String missingVar = e.getKey();

                /*
                * Create a MOEA RealVariable for each missing value
                * and pass it as input to the MultiObjectiveOptimizer
                 */
				Double min = Double.parseDouble(miss[2]);
				Double max = Double.parseDouble(miss[3]);
				RealVariable rv = new RealVariable(min, max);

				missing.put(missingVar, rv);
				MultiObjectiveOptimizer.missingVars.add(missingVar);
				rvs.add(rv);
			}

		}

		if (missing.size() > 0){
			MultiObjectiveOptimizer.variables = rvs;
			MultiObjectiveOptimizer.isp = in;
			MultiObjectiveOptimizer.models = models;
			MultiObjectiveOptimizer.policy = policy;

			/* Set the model to the optimizer according to the optimization policy*/
			try {
				MultiObjectiveOptimizer.model = models.get(policy).get(0);
			}
			catch(Exception e){
				logger.info( e.getMessage());
				logger.info( "Error while trying to get the policy from the model for");
				logger.info( "operator " + operator.opName);
				logger.info( "The policy of the model is null. May this be due to errors");
				logger.info( "during the training of the models. Deleting and retraining");
				logger.info( "the model may resolve the problem. Before retraining make");
				logger.info( "sure that there exist enough samples( profiling data) in the");
				logger.info( "corresponding asapLibrary/operators/OPERATOR_HOME/data/.csv file.");
			}

			Solution solution = findOptimal();
			int numVars = solution.getNumberOfVariables();

			for (int i=0; i<numVars; ++i){
				String missingVar = MultiObjectiveOptimizer.missingVars.get(i);
				Double optimal = Math.floor(Double.parseDouble(solution.getVariable(i).toString()));
				in.addDimension(missingVar, optimal);

				//e.setValue(optimal);
				optree.add("SelectedParam." + missingVar, optimal.toString());

				/* Set the Selected Parameters as execution arguments*/
				String argument = String.format("Execution.Argument%s",
						getExecutionArguments(optree));
				optree.add(argument, optimal.toString());
				Integer argCount = Integer.parseInt(operator.getParameter("Execution.Arguments.number")) + 1;
				optree.add("Execution.Arguments.number", argCount.toString());

			}

		}

        /* TODO: Fix this block */
		OutputSpacePoint out =  new OutputSpacePoint();
		out.setInputSpacePoint(in);
		for(String metric : models.keySet()){
			Double m = operator.getMettric(metric, in);
			out.addValue(metric, m);
		}
        /* --------- */
		return out;
	}

	protected static int getExecutionArguments(SpecTree tree){
		SpecTreeNode args = tree.getNode("Execution");

		if (args == null)
			return 0;
		else {
			int c = 0;
			for (String n : args.children.keySet()){
				if (n.matches("Argument\\d"))
					++c;
			}
			return c;
		}
	}


	/* */
	protected static Solution findOptimal(){
		NondominatedPopulation result = new Executor()
				.withProblemClass(MultiObjectiveOptimizer.class)
				.withAlgorithm("NSGAII")
				.withMaxEvaluations(100)
				.run();

		Double bestTime = result.get(0).getObjective(0);
		Solution solution = result.get(0);
		Double bestParam = Double.parseDouble(result.get(0).getVariable(0).toString());

		return solution;
	}

	protected static Double computePolicyFunction(HashMap<String,Double> metrics, String policy)
			throws NumberFormatException, EvaluationException {
		Evaluator evaluator = new Evaluator();
		Double res;
		String tempFunction = new String(policy);
		for(String m : metrics.keySet()){
			tempFunction=tempFunction.replace(m, metrics.get(m)+"");
		}
		res = Double.parseDouble(evaluator.evaluate(tempFunction));
		return res;
	}

}
