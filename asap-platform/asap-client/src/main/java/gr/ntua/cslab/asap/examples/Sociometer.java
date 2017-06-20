package gr.ntua.cslab.asap.examples;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

/**
 * Created by vic on 3/4/2017.
 */
public class Sociometer {
    public static void main(String[] args) throws Exception {

        ClientConfiguration conf = new ClientConfiguration("asapmaster", 1323);
        WorkflowClient cli = new WorkflowClient();
        cli.setConfiguration(conf);

        AbstractWorkflow1 abstractWorkflow = socioMeter();

        String policy ="metrics,cost,execTime\n"+
                "groupInputs,execTime,max\n"+
                "groupInputs,cost,sum\n"+
                "function,2*execTime+3*cost,min";

        cli.addAbstractWorkflow(abstractWorkflow);
        //cli.materializeWorkflow(abstractWorkflow.name, policy);


		/*String materializedWorkflow = cli.materializeWorkflow("abstractTest1", policy);
		System.out.println(materializedWorkflow);
		cli.executeWorkflow(materializedWorkflow);*/
    }

    public static AbstractWorkflow1 socioMeter() {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("abstractTest1");

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


        Dataset input = new Dataset("REPORT_ESTRAZ_TRAFF_COMUNE_ROMA_20160301");
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
