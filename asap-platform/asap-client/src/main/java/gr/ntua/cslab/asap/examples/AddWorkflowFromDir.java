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


package gr.ntua.cslab.asap.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import gr.ntua.cslab.asap.client.ClientConfiguration;
import gr.ntua.cslab.asap.client.OperatorClient;
import gr.ntua.cslab.asap.client.RestClient;
import gr.ntua.cslab.asap.client.WorkflowClient;
import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Dataset;
import gr.ntua.cslab.asap.operators.NodeName;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

public class AddWorkflowFromDir {
	
	public static void main(String[] args) throws Exception {
		ClientConfiguration conf = new ClientConfiguration(args[0], 1323);
		WorkflowClient cli = new WorkflowClient();
		cli.setConfiguration(conf);
		
		

        String name = args[1];
        String directory = args[2];
		AbstractWorkflow1 abstractWorkflow = new AbstractWorkflow1(name);

		readFromDir(directory, abstractWorkflow);

		cli.addAbstractWorkflow(abstractWorkflow);

		String policy ="metrics,cost,execTime\n"+
						"groupInputs,execTime,max\n"+
						"groupInputs,cost,sum\n"+
						"function,execTime,min";

		String materializedWorkflow = cli.materializeWorkflow(name, policy);
		System.out.println(materializedWorkflow);
		
        //cli.executeWorkflow(materializedWorkflow);
		
	}
	public static void readFromDir(String directory, AbstractWorkflow1 abstractWorkflow) throws IOException {
		HashMap<String,WorkflowNode> nodes = new HashMap<String, WorkflowNode>();
		File folder = new File(directory+"/operators");
		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && !files[i].isHidden()) {
				WorkflowNode n = new WorkflowNode(true, true,"");
				AbstractOperator temp = new AbstractOperator(files[i].getName());
				temp.readPropertiesFromFile(files[i]);
				n.setAbstractOperator(temp);
				nodes.put(temp.opName, n);
			}
		}
		folder = new File(directory+"/datasets");
		/* vpapa: read only if datasets folder exists and it has content */
		if( folder.exists()){
			files = folder.listFiles();
			if( files != null && files.length > 0){
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile() && !files[i].isHidden()) {
						WorkflowNode n =null;
						Dataset temp = new Dataset(files[i].getName());
						temp.readPropertiesFromFile(files[i]);
						int metadatsize =temp.datasetTree.tree.size();
						if(metadatsize==0){
							n = new WorkflowNode(false, true,"");
						}
						else{
							n = new WorkflowNode(false, false, "");
						}
						n.setDataset(temp);
						nodes.put(temp.datasetName, n);
					}
				}
			}
		}
		//putting nodes into workflowNodes make them available for printing at IReS WUI
		abstractWorkflow.workflowNodes.putAll(nodes);
		File edgeGraph = new File(directory+"/graph");
		FileInputStream fis = new FileInputStream(edgeGraph);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		String[] e	= null;
		WorkflowNode src = null;
		WorkflowNode dest = null;
		/* vpapa: operators or datasets defined in graph file may be missing from
			the corresponding folders or misswritten into abstract workflow's graph
			file
		*/
		try{
			while ((line = br.readLine()) != null) {
				e = line.split(",");
				if(e[1].equals("$$target")){
					abstractWorkflow.addTarget(nodes.get(e[0]));
				}
				else if(e.length==2){
					src = nodes.get(e[0]);
					dest = nodes.get(e[1]);
					dest.addInput(src);
					src.addOutput(dest);
				}
				else if(e.length==3){
					src = nodes.get(e[0]);
					dest = nodes.get(e[1]);
					if(dest.isOperator){
						dest.addInput(Integer.parseInt(e[2]), src);
						src.addOutput(dest);
					}
					else{
						dest.addInput(src);
						src.addOutput(Integer.parseInt(e[2]), dest);
					}
				}
			}
		}
		catch( NullPointerException npe){
			System.out.println( "ERROR: The corresponding files of operators or datasets"
								+ " " + e[ 0] + " and " + e[ 1] + " it looks like"
								+ " that they are missing from the relative folders"
								+ "or miswritten into abstract workflow's graph file.");
		}
        catch( IndexOutOfBoundsException iobe){
             System.out.println( "ERROR: Destination: " + dest + " has " + src + "as source"
             					 + " at its input with number " + e[ 2] + ". However, " + dest
             					 + " has " + dest.inputs.size() + " inputs which is less than " + e[ 2] + "."
             					 + " Make sure that the graph file is set appropriately of the"
             					 + " corresponding workflow for this operator i.e. " + dest + ".");
        }
		br.close();
	}
	
}
