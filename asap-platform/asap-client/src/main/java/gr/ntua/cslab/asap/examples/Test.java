package gr.ntua.cslab.asap.examples;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

/**
 * Created by vic on 3/7/2017.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        ClientConfiguration conf = new ClientConfiguration("asapmaster", 1323);
        WorkflowClient cli = new WorkflowClient();
        cli.setConfiguration(conf);

        int c = 4;
        int memory = 4;

        AbstractWorkflow1 abstractWorkflow = Sociometer.socioMeter("sociometer_2");

        String policy = "metrics,cost,execTime\n" +
                "groupInputs,execTime,max\n" +
                "groupInputs,cost,sum\n" +
                "function,execTime,min";

        String params =
                "WindLatestPeakDetectionSparkNested.Optimization.totalCores=" + c*7 + "\n" +
                        "WindLatestPeakDetectionSparkNested.SelectedParam.totalCores=" + c*7 + "\n" +
                        "WindLatestPeakDetectionSparkNested.Optimization.memoryPerNode=" + memory*1024 + "\n" +
                        "WindLatestPeakDetectionSparkNested.SelectedParam.memoryPerNode=" + memory*1024 + "\n" +
                        "WindLatestPeakDetectionSpark.Optimization.totalCores=" + c*7 + "\n" +
                        "WindLatestPeakDetectionSpark.SelectedParam.totalCores=" + c*7 + "\n" +
                        "WindLatestPeakDetectionSpark.Optimization.memoryPerNode=" + memory*1024 + "\n" +
                        "WindLatestPeakDetectionSpark.SelectedParam.memoryPerNode=" + memory*1024 + "\n" +
                        "WindLatestClusteringScikit.Optimization.memory=" + memory*1024 + "\n" +
                        "WindLatestClusteringScikit.SelectedParam.memory=" + memory*1024 + "\n" +
                        "WindLatestClusteringMllib.Optimization.coresPerNode=" + c*7 + "\n" +
                        "WindLatestClusteringMllib.SelectedParam.coresPerNode=" + c*7 + "\n" +
                        "WindLatestClusteringMllib.Optimization.memoryPerNode=" + memory*1024 + "\n" +
                        "WindLatestClusteringMllib.SelectedParam.memoryPerNode=" + memory*1024 + "\n";

        cli.addAbstractWorkflow(abstractWorkflow);
        String name = cli.materializeWorkflowWithParameters(abstractWorkflow.name, policy, params);
        //cli.executeWorkflow(name);
        //cli.waitForCompletion(name);
    }
}
