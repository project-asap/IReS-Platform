package gr.ntua.cslab.asap.examples;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

/**
 * Created by vic on 17/7/2017.
 */
public class TextAnalytics {
    public static void main(String[] args) throws Exception {
        String host = "master";
        String p = "execTime";

        ClientConfiguration conf = new ClientConfiguration(host, 1323);
        WorkflowClient cli = new WorkflowClient();
        cli.setConfiguration(conf);

        String policy = "metrics,cost,execTime\n" +
                "groupInputs,execTime,max\n" +
                "groupInputs,cost,sum\n" +
                "function,"+p+",min";

        AbstractWorkflow1 aw = TextAnalytics("doc201");
        cli.removeAbstractWorkflow(aw.name);
        cli.addAbstractWorkflow(aw);

        String materialized = cli.materializeWorkflow(aw.name, policy);
        cli.executeWorkflow(materialized);
        cli.waitForCompletion(materialized);
    }
    public static AbstractWorkflow1 PosTagger(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("POS_Tagging");

        AbstractOperator PosTaggerOp = new AbstractOperator("POS");
        WorkflowNode PosTagger = new WorkflowNode(true,true,"POS");
        PosTagger.setAbstractOperator(PosTaggerOp);

        Dataset input1 = new Dataset(dataset);
        WorkflowNode inputData1 = new WorkflowNode(false,false, dataset);
        inputData1.setDataset(input1);

        Dataset d1 = new Dataset("d1");
        WorkflowNode posTaggerOut = new WorkflowNode(false, true,"d1");
        posTaggerOut.setDataset(d1);

        PosTagger.addInput(0, inputData1);
        PosTagger.addOutput(0, posTaggerOut);
        posTaggerOut.addInput(0, PosTagger);


        abstractWorkflow.addTarget(posTaggerOut);

        return abstractWorkflow;
    }

    public static AbstractWorkflow1 TextAnalytics(String dataset) {
        AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1("TextAnalyticsTest");

        AbstractOperator PosTaggerOp = new AbstractOperator("POS");
        WorkflowNode PosTagger = new WorkflowNode(true,true,"POS");
        PosTagger.setAbstractOperator(PosTaggerOp);

        AbstractOperator LemmatizerOp = new AbstractOperator("Lemmatizer");
        WorkflowNode Lemmatizer = new WorkflowNode(true,true,"Lemmatizer");
        Lemmatizer.setAbstractOperator(LemmatizerOp);

        AbstractOperator TFIDFOp = new AbstractOperator("TF_IDF");
        WorkflowNode TFIDF = new WorkflowNode(true,true,"TF_IDF");
        TFIDF.setAbstractOperator(TFIDFOp);

        AbstractOperator KMeansOp = new AbstractOperator("k-Means");
        WorkflowNode KMeans = new WorkflowNode(true,true,"k-Means");
        KMeans.setAbstractOperator(KMeansOp);

        Dataset input1 = new Dataset(dataset);
        WorkflowNode inputData1 = new WorkflowNode(false,false, dataset);
        inputData1.setDataset(input1);

        Dataset d1 = new Dataset("d1");
        WorkflowNode posTaggerOut = new WorkflowNode(false, true,"d1");
        posTaggerOut.setDataset(d1);

        Dataset d2 = new Dataset("d2");
        WorkflowNode lemmatizerOut = new WorkflowNode(false, true,"d2");
        lemmatizerOut.setDataset(d2);

        Dataset d3 = new Dataset("d3");
        WorkflowNode TFIDFOut = new WorkflowNode(false, true,"d3");
        TFIDFOut.setDataset(d3);

        Dataset d4 = new Dataset("d4");
        WorkflowNode KMeansOut = new WorkflowNode(false, true,"d4");
        KMeansOut.setDataset(d4);

        PosTagger.addInput(0, inputData1);
        PosTagger.addOutput(0, posTaggerOut);
        posTaggerOut.addInput(0, PosTagger);

        Lemmatizer.addInput(0, posTaggerOut);
        Lemmatizer.addOutput(0, lemmatizerOut);
        lemmatizerOut.addInput(0, Lemmatizer);

        TFIDF.addInput(0, lemmatizerOut);
        TFIDF.addOutput(0, TFIDFOut);
        TFIDFOut.addInput(TFIDF);

        KMeans.addInput(0, TFIDFOut);
        KMeans.addOutput(0, KMeansOut);
        KMeansOut.addInput(0, KMeans);

        abstractWorkflow.addTarget(KMeansOut);

        return abstractWorkflow;
    }

}
