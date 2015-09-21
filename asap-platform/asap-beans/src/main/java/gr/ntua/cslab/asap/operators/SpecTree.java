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

public class SpecTree {

	public TreeMap<String,SpecTreeNode> tree;
	
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
		for(SpecTreeNode n : tree.values()){

			
			if(n.getName().equals("*")){
				return optree2.tree.size()>0;
			}
			else{
				Pattern p = Pattern.compile(n.getName());
				boolean found =false;
				for(SpecTreeNode n1 : optree2.tree.values()){
					Matcher m = p.matcher(n1.getName());
					//System.out.println("checking: "+n.getName()+" "+n1.getName());
					if(m.matches()){
						found =true;
						//System.out.println("found match: "+n.getName()+" "+n1.getName());
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
