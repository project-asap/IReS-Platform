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


package gr.ntua.cslab.asap.rest.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "operator")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperatorDictionary {

	private static final Log logger = LogFactory.getLog( OperatorDictionary.class);

	private String name, cost, execTime, status, isOperator, isAbstract, description, abstractName;
	private boolean isTarget;
	private List<String> input, output;

	public OperatorDictionary() {
		input = new ArrayList<String>();
		output = new ArrayList<String>();
	}

	public OperatorDictionary(String abstractName, String name, String cost, String execTime, String status, String isOperator, String isAbstract, String description, boolean isTarget) {
		this.abstractName=abstractName;
		this.name = name;
		this.cost = cost;
		this.execTime = execTime;
		this.status = status;
		this.isOperator = isOperator;
		this.isAbstract = isAbstract;
		this.description = description;
		this.isTarget = isTarget;
		input = new ArrayList<String>();
		output = new ArrayList<String>();
	}

	public String getExecTime() {
		return execTime;
	}

	public void setExecTime(String execTime) {
		this.execTime = execTime;
	}

	public String getStatus() {
		return status;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIsOperator() {
		return isOperator;
	}

	public void setIsOperator(String isOperator) {
		this.isOperator = isOperator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addOutput(String out) {
		output.add(out);
	}
	
	public void addInput(String in){
		input.add(in);
	}

	public String getNameNoID() {
		return name.substring(0, name.lastIndexOf("_"));
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}

	public List<String> getOutputs() {
		return output;
	}

	public void setOutputs( List<String> output) {
		this.output = output;
	}

	public List<String> getInput() {
		return input;
	}
	public void setInput(List<String> input) {
		this.input = input;
	}
	
	public String getIsAbstract() {
		return isAbstract;
	}

	public void setIsAbstract(String isAbstract) {
		this.isAbstract = isAbstract;
	}

	public String getAbstractName() {
		return abstractName;
	}

	public void setAbstractName(String abstractName) {
		this.abstractName = abstractName;
	}

	/*vpapa: retrieve the engine where the operator will run and for which it is written for
	*/
	public String getEngine(){
		String description = null;
		String engine = null;
		int engine_index = 0;

		description = this.getDescription();
		//to ensure that the property "Constraints.Engine=OperatorEngine" will be
		//at this format and not any else like "Constraints.Engine = OperatorEngine"
        description = description.replaceAll( " ", "" );
        //in case description comes in an html format
        description = description.replaceAll( "<br>", "\n" );
        logger.info( "Description\n\n" + description);
		engine_index = description.indexOf( "Constraints.Engine=");
		logger.info( "Engine index " + engine_index);
		if( engine_index == -1){
			//logger.info( "Operator " + name + "has not any engine specified.");
			return null;
		}
		//engine = Constraints.Engine=OperatorEngine
		engine = description.substring( engine_index, description.indexOf( "\n", engine_index));
        //System.out.println( "Engine " + engine);
		//engine=OperatorEngine
		engine = engine.split( "=")[ 1].trim();
        //System.out.println( "Engine " + engine);
		logger.info( "Operator " + name + " has " + engine + " as specified engine.");

		return engine;
	}

}
