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


package gr.ntua.cslab.asap.operators;

import gr.ntua.cslab.asap.rest.beans.OperatorDescription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.*;

import org.apache.log4j.Logger;

public class SpecTree {

	public TreeMap<String,SpecTreeNode> tree;
	private static Logger logger = Logger.getLogger( SpecTree.class.getName());
	
	public SpecTree() {
		tree= new TreeMap<String,SpecTreeNode>();
	}

	public void add(String key, String value) {
		String curname = key.substring(0, key.indexOf("."));

		SpecTreeNode s = tree.get(curname);
		
		if(s!=null){
			String nextKey = key.substring(key.indexOf(".")+1);
			s.add(nextKey, value);
		}
		else{
			SpecTreeNode temp = new SpecTreeNode(key, value);
			tree.put(temp.getName(),temp);
		}
		
	}

	public void addRegex(NodeName key, String value) {
		if(key.isRegex){
			SpecTreeNode temp = new SpecTreeNode(key, value);
			tree.put(temp.getName(),temp);
		}
		else{
			String curname = key.name.substring(0, key.name.indexOf("."));
			SpecTreeNode s = tree.get(curname);
			if(s!=null){
				String nextKey = key.name.substring(key.name.indexOf(".")+1);
				NodeName nextNodeName = new NodeName(nextKey, key.nextName, false);
				s.addRegex(nextNodeName, value);
			}
			else{
				SpecTreeNode temp = new SpecTreeNode(key, value);
				tree.put(temp.getName(),temp);
			}
		}
	}
	
	@Override
	public String toString() {
		String ret = "{";
		int i=0;
		for(SpecTreeNode n : tree.values()){
			if(i>=1){
				ret+=", ";
			}
			ret += n.toString();
			i++;
		}
		ret+="}";
		return ret;
	}
	
	public void writeToPropertiesFile(String curentPath, Properties props) {
		for(SpecTreeNode n : tree.values()){
			n.writeToPropertiesFile(curentPath, props);
		}
	}

	public String toKeyValues(String curentPath, String ret, String separator) {
		for(SpecTreeNode n : tree.values()){
			ret =n.toKeyValues(curentPath, ret, separator);
		}
		return ret;
	}

	public void toOperatorDescription(OperatorDescription ret) {
		for(SpecTreeNode n : tree.values()){
			n.toOperatorDescription(ret);
		}
	}

	public boolean checkMatch(SpecTree optree2) {
		//materialized operator optree2 
		//logger.info( "CHECKING " + this.getOpName() + " and " +  optree2.getOpName() + " for matching\n");
		//logger.info( this.getOpName() + "  " + tree);
		//logger.info( optree2.getOpName() + "  " + optree2);
		Pattern p = null;
		for(SpecTreeNode n : tree.values()){
			//logger.info( "SPECTREE: " + n.getName() + " " + "VALUE: " + n);
			if(n.getName().equals("*")){
				return optree2.tree.size()>0;
			}
			else{
				//verify that properties of the same group get checked e.g. Constraints with
				//Constraints
				p = Pattern.compile( n.getName());
				boolean found =false;
				for( SpecTreeNode n1 : optree2.tree.values()){
					//logger.info( "OPTREE: " + n1.getName() + " " + "VALUE: " + n1);
					Matcher m = p.matcher( n1.getName());
					//logger.info( "checking: "+n.getName()+" "+n1.getName());
					if( m.matches()){
						found =true;
						//logger.info( "found match: "+n.getName()+" "+n1.getName());
						//check that the properties themselves match
						if(!n.checkMatch(n1)){
							return false;
						}
					}
				}
				if(!found){
					return false;
				}
				/*SpecTreeNode n1 = optree2.tree.get(n.getName());
				if(n1!=null){
					if(!n.checkMatch(n1))
						return false;
				}
				else{
					return false;
				}*/
			}
		}
		return true;
	}
	
	public String getOpName(){
		/* vpapa: return the name of the corresponding operator which expected to have this
			form OpSpecification{Algorithm{(name, operator_name)}
		*/
		String value = "No_Operator_Name_Found";
		for(SpecTreeNode n : tree.values()){
			if( n.toString().indexOf( "OpSpecification") > -1){
				//System.out.println( "Inside getOpName()");
				//keep from 'OpSpecification{Algorithm{(name, operator_name)}' to the end
				value = n.toString().substring( n.toString().indexOf( "OpSpecification"));
				//keep just the 'OpSpecification{Algorithm{(name, operator_name' part
				value = value.substring( 0, value.indexOf( ")}"));
				//System.out.println( value);
				//take the operator_name
				value = value.split( ",")[ 1];
				//System.out.println( value);
				//now only the 'operator_name' part should be with some leading or trailing
				//whitespaces
				if( value.equals( "")){
					//no operator name has been specified
					value = "No_Operator_Name_Found";
				}
				return value.trim();
			}
		}
		return value;
	}

	public SpecTree copyInputToOpSubTree(String prefix, String inout) {
		SpecTree ret =  new SpecTree();

		//System.out.println("adding key: "+prefix);
		if (prefix.contains(".")){
			String curname = prefix.substring(0, prefix.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = tree.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = prefix.substring(prefix.indexOf(".")+1);
				SpecTreeNode temp = s.copyInputToOpSubTree(nextKey,inout);
				if(temp==null)
					return null;
				ret.tree.put(temp.getName(), temp);
			}
			else{
				return null;
			}
		}
		else{//leaf

			//add Input{i} node
			SpecTreeNode s = tree.get(prefix);
			if(s!=null){
				SpecTreeNode temp = s.copyInputToOpSubTree(null,inout);
				
				SpecTreeNode ret1 = new SpecTreeNode(inout);
				for(SpecTreeNode n : temp.children.values()){
					ret1.children.put(n.getName(), n);
				}
				temp.children = new TreeMap<String, SpecTreeNode>();
				temp.children.put(ret1.getName(), ret1);
				
				ret.tree.put(temp.getName(), temp);
				
			}
			else{
				//not found
				return null;
			}
		}
		return ret;
		
		
	}
	

	public SpecTree clone() throws CloneNotSupportedException {
		SpecTree ret =  new SpecTree();
		ret.tree = new TreeMap<String, SpecTreeNode>();
		for(Entry<String, SpecTreeNode> e: tree.entrySet()){
			ret.tree.put(new String(e.getKey()), e.getValue().clone());
		}
		return ret;
	}

	
	public SpecTree copyInputSubTree(String prefix) {
		SpecTree ret =  new SpecTree();

		//System.out.println("adding key: "+prefix);
		if (prefix.contains(".")){
			String curname = prefix.substring(0, prefix.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = tree.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = prefix.substring(prefix.indexOf(".")+1);
				SpecTreeNode temp = s.copyInputSubTree(nextKey);
				if(temp==null)
					return null;
				ret.tree.put(temp.getName(), temp);
			}
			else{
				return null;
			}
		}
		else{//leaf
			SpecTreeNode s = tree.get(prefix);
			if(s!=null){
				SpecTreeNode temp = s.copyInputSubTree(null);
				ret.tree.put(temp.getName(), temp);
			}
			else{
				return null;
			}
		}
		return ret;
	}


	public SpecTreeNode getNode(String key) {
		if (key.contains(".")){
			String curname = key.substring(0, key.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = tree.get(curname);
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = key.substring(key.indexOf(".")+1);
				return s.getNode(nextKey);
			}
			else{
				return null;
			}
		}
		return tree.get(key); 
	}

	
	public String getParameter(String key) {
		if (key.contains(".")){
			String curname = key.substring(0, key.indexOf("."));
			//System.out.println("Checking name: "+curname);
			SpecTreeNode s = tree.get(curname);
			
			if(s!=null){
				//System.out.println("Found!!");
				String nextKey = key.substring(key.indexOf(".")+1);
				return s.getParameter(nextKey);
			}
			else{
				return null;
			}
		}
		return null;
	}

	public void addAll(SpecTree f) {
		for(Entry<String, SpecTreeNode> k : f.tree.entrySet()){
			SpecTreeNode v = this.tree.get(k.getKey());
			if(v!=null){
				v.addAll(k.getValue());
			}
			else{
				this.tree.put(k.getKey(), k.getValue());
			}
		}
	}
}
