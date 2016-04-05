package gr.ntua.cslab.asap.optimization;

import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.Model;
import org.apache.log4j.Logger;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.util.ArrayList;
import java.util.List;

public class MultiObjectiveOptimizer extends AbstractProblem {
    protected static Model model;
    protected static InputSpacePoint isp;
    protected static List<RealVariable> variables;
    protected static List<String> missingVars = new ArrayList<>();
    private static Logger logger = Logger.getLogger(MultiObjectiveOptimizer.class);

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
            f = model.getPoint(isp).getValue();
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