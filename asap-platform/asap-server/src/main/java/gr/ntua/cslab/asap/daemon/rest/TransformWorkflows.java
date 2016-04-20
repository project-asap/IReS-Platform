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


package gr.ntua.cslab.asap.daemon.rest;

import java.io.IOException;

import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.AbstractWorkflow1;

public class TransformWorkflows {

	public static AbstractWorkflow1 tranformAbstractWorkflow(String name, String dir, WorkflowDictionary workflow) throws IOException {
		

		AbstractWorkflow1 ret = new AbstractWorkflow1(name, dir+"/"+name);
		String graph="", targets="";
		for(OperatorDictionary op: workflow.getOperators()){
			if(op.getIsOperator().equals("true") && op.getIsAbstract().equals("true"))
				ret.addNode("1", op.getName());
			else if(op.getIsOperator().equals("true") && op.getIsAbstract().equals("false"))
				ret.addNode("2", op.getName());
			else if(op.getIsOperator().equals("false") && op.getIsAbstract().equals("true"))
				ret.addNode("3", op.getName());
			else if(op.getIsOperator().equals("false") && op.getIsAbstract().equals("false"))
				ret.addNode("4", op.getName());
			
			int i=0;
			if(op.getIsOperator().equals("true")){
				for(String in: op.getInput()){
					graph+=in+","+op.getName()+","+i+"\n";
					//graph+=in+","+op.getName()+"\n";
					i++;
				}	
				i=0;
				for(String out: op.getOutputs()){
					graph+=op.getName()+","+out+","+i+"\n";
					//graph+=in+","+op.getName()+"\n";
					i++;
				}	
			}
			if(op.isTarget())
				targets+=op.getName()+",$$target\n";
		}
		//System.out.println(graph);
		graph+=targets;
		ret.changeEdges(graph);
		return ret;
	}

}
