package gr.ntua.cslab.asap.examples;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;
import net.sourceforge.jeval.function.math.Abs;
import net.sourceforge.jeval.function.math.Exp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by vic on 3/4/2017.
 */
public class Sociometer {
    public static void main(String[] args) throws Exception {
        int coresStart = Integer.parseInt(args[4]);
        int coresEnd = Integer.parseInt(args[5]);
        int memStart = Integer.parseInt(args[6]);
        int memEnd = Integer.parseInt(args[7]);

        runMultipleExperiments(args[0], args[1], args[2], args[3], coresStart, coresEnd, memStart, memEnd);
    }

    public static void runMultipleExperiments(String host, String workflow, String ds, String p,
                                              int coresStart, int coresEnd, int memStart, int memEnd) throws Exception{
        ClientConfiguration conf = new ClientConfiguration(host, 1323);
        WorkflowClient cli = new WorkflowClient();
        cli.setConfiguration(conf);

        FileWriter resultsFile = new FileWriter("results.csv", true);

        for (int memory=memStart; memory<=memEnd; memory++) {
            for (int c=coresStart; c<=coresEnd; c++) {
                AbstractWorkflow1 abstractWorkflow;

                switch (workflow) {
                    case "kmeans": abstractWorkflow = KMeans(ds);
                        break;
                    case "sociometer": abstractWorkflow = socioMeter(ds);
                        break;
                    case "peakdetection": abstractWorkflow = PeakDetection(ds);
                        break;
                    case "profiling": abstractWorkflow = userProfiling(ds);
                        break;
                    case "pk": abstractWorkflow = ProfilingAndKMeans(ds);
                        break;
                    case "textanalytics": abstractWorkflow = TextAnalytics.TextAnalytics(ds);
                        break;
                    default: throw new Exception("Workflow does not exists");
                }

                String policy = "metrics,cost,execTime\n" +
                        "groupInputs,execTime,max\n" +
                        "groupInputs,cost,sum\n" +
                        "function,"+p+",min";

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

                long startTime = System.currentTimeMillis();
                cli.executeWorkflow(name);
                cli.waitForCompletion(name);
                double endTime = (System.currentTimeMillis() - startTime) / 1000.0;

                resultsFile.append(String.format("%s, %s, %s, %d, %d, %f\n", workflow, ds, p, c, memory, endTime));
                resultsFile.flush();
            }
        }
        resultsFile.close();
    }

    public static AbstractWorkflow1 PeakDetection(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("PeakDetection");

        AbstractOperator peakDetectionOp = new AbstractOperator("Wind_Latest_Peak_Detection");
        WorkflowNode peakDetection = new WorkflowNode(true,true,"Wind_Latest_Peak_Detection");
        peakDetection.setAbstractOperator(peakDetectionOp);

        Dataset input1 = new Dataset("annotation_global");
        WorkflowNode inputData1 = new WorkflowNode(false,false, "annotation_global");
        inputData1.setDataset(input1);

        Dataset input2 = new Dataset(dataset);
        WorkflowNode inputData2 = new WorkflowNode(false,false, dataset);
        inputData2.setDataset(input2);

        Dataset d1 = new Dataset("d1");
        WorkflowNode peakDetectionOut = new WorkflowNode(false, true,"d1");
        peakDetectionOut.setDataset(d1);

        peakDetection.addInput(0, inputData1);
        peakDetection.addInput(1, inputData2);
        peakDetection.addOutput(0, peakDetectionOut);
        peakDetectionOut.addInput(0, peakDetection);


        abstractWorkflow.addTarget(peakDetectionOut);

        return abstractWorkflow;
    }

    public static AbstractWorkflow1 ProfilingAndKMeans(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("ProfilingAndKMeans");

        AbstractOperator clusteringOp = new AbstractOperator("Wind_Latest_Kmeans");
        WorkflowNode clustering = new WorkflowNode(true,true,"Wind_Latest_Kmeans");
        clustering.setAbstractOperator(clusteringOp);

        AbstractOperator userProfilingOp = new AbstractOperator("Wind_Latest_User_Profiling");
        WorkflowNode userProfiling = new WorkflowNode(true,true,"Wind_Latest_User_Profiling");
        userProfiling.setAbstractOperator(userProfilingOp);

        Dataset input = new Dataset(dataset);
        WorkflowNode inputData = new WorkflowNode(false,false, dataset);
        inputData.setDataset(input);

        Dataset d1 = new Dataset("d1");
        WorkflowNode profilingOut = new WorkflowNode(false, true,"d1");
        profilingOut.setDataset(d1);

        Dataset d2 = new Dataset("d2");
        WorkflowNode clusteringOut = new WorkflowNode(false, true,"d2");
        clusteringOut.setDataset(d2);

        userProfiling.addInput(0, inputData);
        userProfiling.addOutput(0, profilingOut);
        profilingOut.addInput(0, userProfiling);

        clustering.addInput(0, profilingOut);
        clustering.addOutput(0, clusteringOut);
        clusteringOut.addInput(0, clustering);


        abstractWorkflow.addTarget(clusteringOut);

        return abstractWorkflow;
    }

    public static AbstractWorkflow1 KMeans(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("KMeans");

        AbstractOperator clusteringOp = new AbstractOperator("Wind_Latest_Kmeans");
        WorkflowNode clustering = new WorkflowNode(true,true,"Wind_Latest_Kmeans");
        clustering.setAbstractOperator(clusteringOp);

        Dataset input = new Dataset(dataset);
        WorkflowNode inputData = new WorkflowNode(false,false, dataset);
        inputData.setDataset(input);

        Dataset d1 = new Dataset("d1");
        WorkflowNode clusteringOut = new WorkflowNode(false, true,"d1");
        clusteringOut.setDataset(d1);

        clustering.addInput(0, inputData);
        clustering.addOutput(0, clusteringOut);
        clusteringOut.addInput(0, clustering);


        abstractWorkflow.addTarget(clusteringOut);

        return abstractWorkflow;
    }

    public static AbstractWorkflow1 userProfiling(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("UserProfiling");

        AbstractOperator userProfilingOp = new AbstractOperator("Wind_Latest_User_Profiling");
        WorkflowNode userProfiling = new WorkflowNode(true,true,"Wind_Latest_User_Profiling");
        userProfiling.setAbstractOperator(userProfilingOp);

        Dataset input = new Dataset(dataset);
        WorkflowNode inputData = new WorkflowNode(false,false, dataset);
        inputData.setDataset(input);

        Dataset d1 = new Dataset("d1");
        WorkflowNode userProfilingOut = new WorkflowNode(false, true,"d1");
        userProfilingOut.setDataset(d1);

        userProfiling.addInput(0, inputData);
        userProfiling.addOutput(0, userProfilingOut);
        userProfilingOut.addInput(0, userProfiling);


        abstractWorkflow.addTarget(userProfilingOut);

        return abstractWorkflow;
    }

    public static AbstractWorkflow1 socioMeter(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("SocioMeterTest");

        AbstractOperator userProfilingOp = new AbstractOperator("Wind_Latest_User_Profiling");
        WorkflowNode userProfiling = new WorkflowNode(true,true,"Wind_Latest_User_Profiling");
        userProfiling.setAbstractOperator(userProfilingOp);

        AbstractOperator kmeansAbstract = new AbstractOperator("Wind_Latest_Kmeans");
        WorkflowNode kmeans = new WorkflowNode(true,true,"Wind_Latest_Kmeans");
        kmeans.setAbstractOperator(kmeansAbstract);

        AbstractOperator stereoTypeClassificationAbstract = new AbstractOperator("Wind_Latest_Stereo_Type_Classification");
        WorkflowNode stereoTypeClassification = new WorkflowNode(true,true,"Wind_Latest_Stereo_Type_Classification");
        stereoTypeClassification.setAbstractOperator(stereoTypeClassificationAbstract);

        AbstractOperator socioPublisherAbstract = new AbstractOperator("Wind_Latest_Socio_Publisher");
        WorkflowNode socioPublisher = new WorkflowNode(true,true,"Wind_Latest_Socio_Publisher");
        socioPublisher.setAbstractOperator(socioPublisherAbstract);

        AbstractOperator ODMatrixAbstract = new AbstractOperator("Wind_Latest_OD_Matrix");
        WorkflowNode ODMatrix = new WorkflowNode(true,true,"Wind_Latest_OD_Matrix");
        ODMatrix.setAbstractOperator(ODMatrixAbstract);

        AbstractOperator ODMatrixPublisherAbstract = new AbstractOperator("Wind_Latest_ODMatrix_Publisher");
        WorkflowNode ODMatrixPublisher = new WorkflowNode(true,true,"Wind_Latest_ODMatrix_Publisher");
        ODMatrixPublisher.setAbstractOperator(ODMatrixPublisherAbstract);

        AbstractOperator peakDetectionAbstract = new AbstractOperator("Wind_Latest_Peak_Detection");
        WorkflowNode peakDetection = new WorkflowNode(true,true,"Wind_Latest_Peak_Detection");
        peakDetection.setAbstractOperator(peakDetectionAbstract);

        AbstractOperator peakPublisherAbstract = new AbstractOperator("Wind_Latest_Peak_Publisher");
        WorkflowNode peakPublisher = new WorkflowNode(true,true,"Wind_Latest_Peak_Publisher");
        peakPublisher.setAbstractOperator(peakPublisherAbstract);

        AbstractOperator weblyzardUploaderAbstract = new AbstractOperator("Wind_Latest_Weblyzard_Uploader");
        WorkflowNode weblyzardUploader = new WorkflowNode(true,true,"Wind_Latest_Weblyzard_Uploader");
        weblyzardUploader.setAbstractOperator(weblyzardUploaderAbstract);


        Dataset input = new Dataset(dataset);
        WorkflowNode inputData = new WorkflowNode(false,false,"WIND_DATASET");
        inputData.setDataset(input);

        Dataset d1 = new Dataset("d1");
        WorkflowNode userProfilingOut = new WorkflowNode(false, true,"d1");
        userProfilingOut.setDataset(d1);

        Dataset d2 = new Dataset("d2");
        WorkflowNode kmeansOut = new WorkflowNode(false, true, "d2");
        kmeansOut.setDataset(d2);

        Dataset d3 = new Dataset("d3");
        WorkflowNode stereoTypeOut = new WorkflowNode(false, true, "d3");
        stereoTypeOut.setDataset(d3);

        Dataset d8 = new Dataset("d8");
        WorkflowNode stereoTypeOut2 = new WorkflowNode(false, true, "d8");
        stereoTypeOut2.setDataset(d8);

        Dataset d4 = new Dataset("d4");
        WorkflowNode socioPublisherOut = new WorkflowNode(false, true, "d4");
        socioPublisherOut.setDataset(d4);

        Dataset d5 = new Dataset("d5");
        WorkflowNode weblyzardUploaderOut = new WorkflowNode(false, true, "d5");
        weblyzardUploaderOut.setDataset(d5);

        Dataset d6 = new Dataset("d6");
        WorkflowNode ODMatrixOut = new WorkflowNode(false, true, "d6");
        ODMatrixOut.setDataset(d6);

        Dataset d7 = new Dataset("d7");
        WorkflowNode ODMatrixPublisherOut = new WorkflowNode(false, true, "d7");
        ODMatrixPublisherOut.setDataset(d7);


        Dataset d9 = new Dataset("d9");
        WorkflowNode peakDetectionOut = new WorkflowNode(false, true, "d9");
        peakDetectionOut.setDataset(d9);

        Dataset d10 = new Dataset("d10");
        WorkflowNode peakPublisherOut = new WorkflowNode(false, true, "d10");
        peakPublisherOut.setDataset(d10);


        //Operator input-output
        userProfiling.addInput(0, inputData);
        userProfiling.addOutput(0, userProfilingOut);
        userProfilingOut.addInput(0, userProfiling);

        ODMatrix.addInput(0, stereoTypeOut);
        ODMatrix.addInput(1, inputData);
        ODMatrix.addOutput(0, ODMatrixOut);
        ODMatrixOut.addInput(0, ODMatrix);

        ODMatrixPublisher.addInput(0, ODMatrixOut);
        ODMatrixPublisher.addOutput(0, ODMatrixPublisherOut);
        ODMatrixPublisherOut.addInput(0, ODMatrixPublisher);

        kmeans.addInput(0, userProfilingOut);
        kmeans.addOutput(0, kmeansOut);
        kmeansOut.addInput(0, kmeans);

        stereoTypeClassification.addInput(0, userProfilingOut);
        stereoTypeClassification.addInput(1, kmeansOut);
        stereoTypeClassification.addOutput(0, stereoTypeOut);
        stereoTypeClassification.addOutput(1, stereoTypeOut2);

        stereoTypeOut.addInput(0, stereoTypeClassification);
        stereoTypeOut2.addInput(0, stereoTypeClassification);

        socioPublisher.addInput(0, stereoTypeOut2);
        socioPublisher.addOutput(0, socioPublisherOut);
        socioPublisherOut.addInput(0, socioPublisher);

        peakDetection.addInput(0, stereoTypeOut);
        peakDetection.addInput(1, inputData);
        peakDetection.addOutput(0, peakDetectionOut);
        peakDetectionOut.addInput(0, peakDetection);

        peakPublisher.addInput(0, peakDetectionOut);
        peakPublisher.addOutput(0, peakPublisherOut);
        peakPublisherOut.addInput(0, peakPublisher);

        weblyzardUploader.addInput(0, socioPublisherOut);
        weblyzardUploader.addInput(1, ODMatrixPublisherOut);
        weblyzardUploader.addInput(2, peakPublisherOut);

        weblyzardUploader.addOutput(0, weblyzardUploaderOut);
        weblyzardUploaderOut.addInput(0, weblyzardUploader);

        abstractWorkflow.addTarget(weblyzardUploaderOut);

        return abstractWorkflow;
    }
}
