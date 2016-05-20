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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "operator")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperatorDescription {
	private String name, value;
	private List<OperatorDescription> children;
	
	public OperatorDescription(String name, String value) {
		this.name = name;
		this.value = value;
		children = new ArrayList<OperatorDescription>();
	}

	public OperatorDescription() {
		this( "","");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public void addChild(OperatorDescription child){
		children.add(child);
	}
	
	public static OperatorDescription getTest(){
		OperatorDescription op1 = new OperatorDescription("l1.1.1", "v1");
		OperatorDescription op2 = new OperatorDescription("l1.1.2", "v2");
		OperatorDescription op3 = new OperatorDescription("l1.2.1", "v3");
		OperatorDescription op4 = new OperatorDescription("l1.3.1", "v4");
		OperatorDescription op5 = new OperatorDescription("l1.3.2", "v5");
		OperatorDescription op6 = new OperatorDescription("l1.3.3", "v6");

		OperatorDescription op7 = new OperatorDescription("l1.1", "");
		op7.addChild(op1);
		op7.addChild(op2);
		

		OperatorDescription op8 = new OperatorDescription("l1.2", "");
		op8.addChild(op3);
		
		OperatorDescription op9 = new OperatorDescription("l1.3", "");
		op9.addChild(op4);
		op9.addChild(op5);
		op9.addChild(op6);

		OperatorDescription op10 = new OperatorDescription("l1", "");
		op10.addChild(op7);
		op10.addChild(op8);
		op10.addChild(op9);
		
		return op10;
	}
}
