package gr.ntua.cslab.asap.optimization;

import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.operators.SpecTree;
import gr.ntua.cslab.asap.operators.SpecTreeNode;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Workflow missing metrics multi-objective optimization
 */

public class OptimizeMissingMetrics {
	private static Logger logger = Logger.getLogger(OptimizeMissingMetrics.class.getName());

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

			/* Set the model to the optimizer according to the optimization policy*/
			try {
				MultiObjectiveOptimizer.model = models.get(policy).get(0);
			}
			catch(Exception e){
				this.logger.info(e.getMessage());
				this.logger.info("Error while trying to get the policy from the model.");
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
				/*String argument = String.format("Execution.Argument%s",
						getExecutionArguments(optree));
				optree.add(argument, optimal.toString());
				Integer argCount = Integer.parseInt(operator.getParameter("Execution.Arguments.number")) + 1;
				optree.add("Execution.Arguments.number", argCount.toString());*/

			}

			System.out.println("Parameter: "+optree.getParameter("SelectedParam.cores"));


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

	protected Double computePolicyFunction(HashMap<String,Double> metrics, String policy) throws NumberFormatException, EvaluationException {
		Evaluator evaluator = new Evaluator();
		Double res=0.0;
		String tempFunction = new String(policy);
		for(String m : metrics.keySet()){
			tempFunction=tempFunction.replace(m, metrics.get(m)+"");
		}
		res = Double.parseDouble(evaluator.evaluate(tempFunction));
		return res;
	}

}
