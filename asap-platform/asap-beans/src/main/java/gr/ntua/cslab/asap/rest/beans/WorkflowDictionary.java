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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowDictionary {
	
	private static Logger logger = Logger.getLogger( WorkflowDictionary.class.getName());
	
	List<OperatorDictionary> operators;
	private String name = null;
	private HashMap< String, Integer> indexes = null;
	private HashMap< String, ArrayList< String>> graph = null;
	public List< String> failedops = null;
	public boolean isUpdated = false;

	public WorkflowDictionary() {
		this( "");
	}
	
	public WorkflowDictionary( String name) {
		operators = new ArrayList<OperatorDictionary>();
		this.name = name;
		indexes = new HashMap< String, Integer>();
		graph = new HashMap< String, ArrayList< String>>();
	}	

	public void addOperator(OperatorDictionary op){
		operators.add(op);
	}
	
	public List<OperatorDictionary> getOperators() {
		return operators;
	}

	public void setOperators(List<OperatorDictionary> operators) {
		this.operators = operators;
	}

	public OperatorDictionary getOperator(String name) {
		for(OperatorDictionary op: operators){
			if(op.getName().equals(name))
				return op;
		}
		return null;
	}

	public void setOutputsRunning(String name, String status) {
		if( status == null){
			status = "running";
		}
		for(OperatorDictionary op: operators){
			if(op.getIsOperator().equals("false") && ( op.getStatus().equals("warn") || op.getStatus().equals("running"))){
				for(String op1 : op.getInput()){
					if(op1.equals(name) ){
						op.setStatus( status);
						setOutputsRunning(op.getName(), status);
					}
				}
			}
		}
	}

	public void replaceDescription(String target, String replacement) {
		for(OperatorDictionary op : operators){
			op.setDescription(op.getDescription().replace(target, replacement));
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName( String name){
		this.name = name;
	}
	
	/**
	 * Returns the position of each node inside the WorkflowDictionary
	 * as a HashMap
	 * 
	 * @author Vassilis Papaioannou
	 * @return graph The graph representation
	 */
	public HashMap< String, Integer> getIndexes(){
		if( indexes.isEmpty()){
			for( int i = 0; i < operators.size(); i++){
				indexes.put( operators.get( i).getName(), i);
			}			
		}
		return indexes;
	}
	
	/**
	 * Returns the graph representation of the WorkflowDictionary
	 * as a HashMap of ArrayLists
	 * 
	 * @author Vassilis Papaioannou
	 * @return graph The graph representation
	 */
	public HashMap< String, ArrayList< String>> getGraph(){
		if( graph.isEmpty()){
			return createGraph();
		}
		return graph;
	}
	
	/**
	 * Creates the graph representation of the WorkflowDictionary
	 * as a HashMap of ArrayLists
	 * 
	 * @author Vassilis Papaioannou
	 * @return graph The graph representation
	 */
	private HashMap< String, ArrayList< String>> createGraph(){
		Iterator< String> lis = null;
		
		//intialize graph
	    for( OperatorDictionary opdic : operators){
	    	graph.put( opdic.getName(), new ArrayList< String>());
	    }
		
	    for( OperatorDictionary opdic : operators){

	    	if( !opdic.getInput().isEmpty()){
	    		lis = opdic.getInput().listIterator();
	    		while( lis.hasNext()){
	    			graph.get( lis.next()).add( opdic.getName());
	    		}
	    	}
	    }
	    return graph;
	}

	/**
	 * Returns a list of the output nodes of operator opname
	 * 
	 * @author Vassilis Papaioannou
	 * @param opname the operator name of the node for which its output nodes are needed
	 * @return outputlist the list of the output nodes
	 */
	public List< String> getOutputs( String opname){
	    return getGraph().get( opname);
	}
	
	/**
	 * Updates WorkflowDictionary's nodes one by one by traversing the WorkflowDictionary recursively from the
	 * last node to the start of the original WorkflowDictionary. The update is driven by the WorkflowDictionary
	 * updateswd that contains the new content for each node that will be updated. 
	 * 
	 * @author Vassilis Papaioannou
	 * @param targets 	the names of the nodes that will be updated
	 * @param updates 	the names of the nodes that will substitute the targets nodes
	 * @param updateswd the WorkflowDictionary according to which the update will take place
	 * @param starting	the common node between the original WorkflowDictionary and the updateswd.
	 * @return updated	a list of operators that have been updated
	 */
	private void updateNodes( List< String> targets, List< String> updates, WorkflowDictionary updateswd, String starting){
		String description = null;
		List< String> newtargets = new ArrayList< String>();
		List< String> newupdates = new ArrayList< String>();
		List< String> filterout = new ArrayList< String>();
		
		
		for( String s : targets){
			logger.info( "TARGETS: " + s);
		}
		for( String s : updates){
			logger.info( "UPDATES: " + s);
		}
		
		
		if( updates == null || updates.isEmpty()){
			logger.info( "UPDATES is either null or empty!");
			return;
		}
		
		for( int i = 0; i < updates.size(); i++){
			//prepare to update next nodes that are the input of the current nodes
			newtargets.addAll(( operators.get( indexes.get( targets.get( i))).getInput()));
			newupdates.addAll(( updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))).getInput()));
			//update current nodes
			//1. set their status to 'warn' to enable them to run if their status in 'updateswd' workflow is running
			if( updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))).getStatus().equals( "running")){
				updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))).setStatus( "warn");	
			}
			//2. format their description to enable their description to be read properly
			description = updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))).getDescription().replace( "<br>", "\n");
			updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))).setDescription( description);
			//when the starting node of updates WorkflowDictionary is found, do not update it, in order to keep the connection
			//with the already executed part of the workflow and since this node already exists
			if( !updates.get( i).equals( starting)){
				operators.set( indexes.get( targets.get( i)), updateswd.getOperators().get( updateswd.getIndexes().get( updates.get( i))));
			}
			else {
				//set the output dataset of the starting node in status 'completed'
				this.setOutputsRunning( starting, "completed");
			}
		}
		//filter out operators from the 'newtargets' list that 
		//1. have status 'failed'
		for( String s : newtargets){
			if( operators.get( indexes.get( s)).getStatus().equals( "failed")){
				//'filterout' list is used instead of 'newtargets.remove()' command to avoid
				//java.util.ConcurrentModificationException
				filterout.add( s);
			}
		}
		newtargets.removeAll( filterout);
		//2. coexist with starting node of the updateswd WorkflowDictionary
		if( newtargets.contains( starting)){
			newtargets.clear();
			newtargets.add( starting);
		}
		//filter out 'starting' operator from the 'newupdates' list because it is
		//already placed inside the updated workflow
		while( newupdates.indexOf( starting) != newupdates.lastIndexOf( starting)){
			newupdates.remove( starting);
		}
		
		updateNodes( newtargets, newupdates, updateswd, starting);
		return;
	}
	
	/**
	 * Initiates the process of updating the WorkflowDictionary with the nodes of WorkflowDictionary updates.
	 * 
	 * @author Vassilis Papaioannou
	 * @param updates the WorkflowDictionary according to which the WorkflowDictionary will be updated
	 * @param conf the configuration of the running workflow
	 * @return updated	a list of operators that have been updated
	 */
	public void initiateUpdate( WorkflowDictionary updates) {
		List< String> newtargets = new ArrayList< String>();
		List< String> newupdates = new ArrayList< String>();
		Iterator< String> lis = null;
		
		if( updates == null || updates.getOperators().isEmpty()){
			logger.info( "The workflow " + this.getName() + " did not get updated because no updates were given");
			isUpdated = false;
			return;
		}
		
		//update the status of the 'failed' operators due to lack of alternative
		if( !( updates.failedops == null || updates.failedops.isEmpty())){
			for( String fop : updates.failedops){
				logger.info( "FAILED OPERATOR: " + fop);
				for( OperatorDictionary opdic : getOperators()){
					logger.info( "OPERATOR ABSTRACT NAME: " + opdic.getAbstractName() + "\tOPERATOR NAME: " + opdic.getName());
					if( opdic.getAbstractName().equals( fop) && opdic.getStatus().equals( "completed")){
						opdic.setStatus( "failed");
						lis = getOutputs( opdic.getName()).listIterator();
						while( lis.hasNext()){
							String s = lis.next();
							logger.info( "OUTPUT OPERATOR ABSTRACT NAME: " + getOperator( s).getAbstractName() + "\tOPERATOR NAME: " + getOperator( s).getName());
							getOperator( s).setStatus( "failed");
						}
						break;
					}
				}
			}
		}
		
		//get a mapping between operators and their position inside the WorkflowDictionary
		getIndexes();
		newtargets.add( operators.get( operators.size() - 1).getName());
		newupdates.add( updates.getOperators().get( updates.getOperators().size() - 1).getName());
		updateNodes( newtargets, newupdates, updates, updates.getOperators().get( 0).getName());
		isUpdated = true;
	
		//the WorfkflowDictionary has been changed, for this its 'indexes' and 'graph' should be updated
		indexes.clear();
		getIndexes();
		createGraph();
		
		return;
	}
}
