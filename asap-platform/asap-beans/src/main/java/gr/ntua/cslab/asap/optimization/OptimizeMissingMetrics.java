package gr.ntua.cslab.asap.optimization;

import gr.ntua.cslab.asap.operators.SpecTree;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.variable.RealVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Workflow missing metrics multi-objective optimization
 */

public class OptimizeMissingMetrics {
	public static OutputSpacePoint findOptimalPointCheckAllSamples(
			HashMap<String, List<Model>> models, InputSpacePoint in,
			String policy, SpecTree optree) throws Exception {

		List<RealVariable> rvs = new ArrayList<>();

        MultiObjectiveOptimizer.missingVars.clear();

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
                MultiObjectiveOptimizer.missingVars.add(missingVar);

                /*
                * Create a MOEA RealVariable for each missing value
                * and pass it as input to the MultiObjectiveOptimizer
                 */
				Double min = Double.parseDouble(miss[2]);
				Double max = Double.parseDouble(miss[3]);
                RealVariable rv = new RealVariable(min, max);
                rvs.add(rv);

                MultiObjectiveOptimizer.variables = rvs;
                MultiObjectiveOptimizer.isp = in;

                //TODO: Fix this loop
				for (Entry<String, List<Model>> m : models.entrySet()) {
                    MultiObjectiveOptimizer.model = m.getValue().get(0);
				}

                Double optimal = findOptimal();
                if (optimal < 0) continue;
                in.addDimension(missingVar, optimal);
				e.setValue(optimal);
				optree.add("SelectedParam." + missingVar, optimal.toString());
			}

		}

        /* TODO: Fix this block */
		OutputSpacePoint out =  new OutputSpacePoint();
		out.setInputSpacePoint(in);
		for(String metric : models.keySet()){
			Model model = models.get(metric).get(0);
			if(!model.getClass().equals(
					gr.ntua.ece.cslab.panic.core.models.UserFunction.class)){
				for(Model m:models.get(metric)){

					if(m.getInputSpace().size()>=2 && m.getClass()
                            .equals(gr.ntua.ece.cslab.panic.core.models.MLPerceptron.class)){
						model =m;
						break;
					}
					if(m.getInputSpace().size()<2 && m.getClass().equals(
							gr.ntua.ece.cslab.panic.core.models.LinearRegression.class)){
						model =m;
						break;
					}
				}
			}

			OutputSpacePoint op =  new OutputSpacePoint();
			HashMap<String, Double> values = new HashMap<String, Double>();
			for(String k :  model.getOutputSpace().keySet()){
				values.put(k, null);
			}
			op.setValues(values);
			OutputSpacePoint res = model.getPoint(in,op);
			out.addValue(metric, res.getOutputPoints().get(metric));
		}
        /* --------- */
		return out;
	}

    /* */
    protected static Double findOptimal(){
        NondominatedPopulation result = new Executor()
                .withProblemClass(MultiObjectiveOptimizer.class)
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(100)
                .run();

        Double bestTime = result.get(0).getObjective(0);
        Double bestParam = Double.parseDouble(result.get(0).getVariable(0).toString());

        /**
         *  TODO (Fix): PANIC in some cases returns negative values as estimated execution time
         **/
        if (bestTime < 0) {
            return -1.0;
        }

        return Math.floor(bestParam);
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
