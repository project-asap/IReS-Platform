package com.cloudera.kitten.appmaster.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;

import com.cloudera.kitten.ContainerLaunchContextFactory;
import com.cloudera.kitten.ContainerLaunchParameters;
import com.google.common.collect.Maps;

public class ContainerTracker implements NMClientAsync.CallbackHandler {
	private static final Log LOG = LogFactory.getLog(ContainerTracker.class);
    public final ContainerLaunchParameters params;
    private final ConcurrentMap<ContainerId, Container> containers = Maps.newConcurrentMap();

    private AtomicInteger needed = new AtomicInteger();
    private AtomicInteger started = new AtomicInteger();
    public AtomicInteger completed = new AtomicInteger();
    private AtomicInteger failed = new AtomicInteger();
    private NMClientAsync nodeManager;
    private Resource resource;
    private Priority priority;
    private ContainerLaunchContext ctxt;
    private List<ContainerTracker> nextTrackers;
    private List<ContainerTracker> previousTrackers;
    public boolean isInitilized;
    private List<AMRMClient.ContainerRequest> containerRequests;
	private WorkflowService service;
    
    public ContainerTracker(WorkflowService service, ContainerLaunchParameters parameters) {
    	this.service = service;
      this.params = parameters;
      this.nextTrackers = new ArrayList<ContainerTracker>();
      this.previousTrackers = new ArrayList<ContainerTracker>();
      needed.set(1);
      isInitilized=false;
    }

    public void addNextTracker(ContainerTracker tracker){
    	this.nextTrackers.add(tracker);
//		LOG.info("NextTrackers for: " +params.getName());
//    	for(ContainerTracker t:nextTrackers){
//    		LOG.info("Tracker: " +t.params.getName());
//    	}
    }

    public void addPreviousTracker(ContainerTracker tracker){
    	this.previousTrackers.add(tracker);
    }
    
    private boolean allPreviousFinished(){
    	boolean ret = true;
    	
    	for(ContainerTracker tracker : previousTrackers){
    		if(tracker.needsContainers()){
    			ret=false;
    			break;
    		}
    	}
    	
    	return ret;
    }
    
    public void init(ContainerLaunchContextFactory factory) throws IOException {
    	if(!allPreviousFinished())
    		return;
    	service.parameters.workflow.getOperator(params.getName()).setStatus("running");
      this.nodeManager = NMClientAsync.createNMClientAsync(this);
      nodeManager.init(service.conf);
      nodeManager.start();
      isInitilized=true;
      
      this.resource = factory.createResource(params);

      //this.priority = factory.createPriority(params.getPriority());
      
      //hack for https://issues.apache.org/jira/browse/YARN-314
      this.priority = factory.createPriority(service.prior);
      service.prior++;
      //hack for https://issues.apache.org/jira/browse/YARN-314
      
      int numInstances = params.getNumInstances();
      LOG.info("Operator: "+params.getName()+" requesting " + numInstances+" containers");
      LOG.info("Resource cores: "+ resource.getVirtualCores());
      LOG.info("Resource memory: "+ resource.getMemory());
      String[] nodes =params.getNodes();//= {"slave1"};
      String labels = params.getLabels();
      AMRMClient.ContainerRequest containerRequest=null;
      if(labels==null){
    	  LOG.info("Resource nodes: all");
      	  containerRequest = new AMRMClient.ContainerRequest(
              resource,
              nodes, // nodes
              null, // racks
              priority,
              true, //true for relaxed locality
              "");
      }
      else{
    	  LOG.info("Resource labels: "+ labels);
    	  for (int i = 0; i < nodes.length; i++) {
        	  LOG.info("Resource nodes: "+ nodes[i]);
    	  }
    	  containerRequest = new AMRMClient.ContainerRequest(
    	          resource,
    	          nodes, // nodes
    	          null, // racks
    	          priority,
    	          false, //true for relaxed locality
    	          "");
      }
      
      this.containerRequests = new ArrayList<AMRMClient.ContainerRequest>();
      //restartResourceManager();
      for (int j = 0; j < numInstances; j++) {
    	  service.resourceManager.addContainerRequest(containerRequest);
    	  containerRequests.add(containerRequest);
      }

      needed.set(numInstances);
    }

    @Override
    public void onContainerStarted(ContainerId containerId, Map<String, ByteBuffer> allServiceResponse) {
	  Container container = containers.get(containerId);
	  if (container != null) {
	    LOG.info("Starting container id = " + containerId);
	    started.incrementAndGet();
	    nodeManager.getContainerStatusAsync(containerId, container.getNodeId());
	  }
    }

    @Override
    public void onContainerStatusReceived(ContainerId containerId, ContainerStatus containerStatus) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received status for container: " + containerId + " = " + containerStatus);
      }
    }

    @Override
    public void onContainerStopped(ContainerId containerId) {
      LOG.info("Stopping container id = " + containerId);
      Container v = containers.remove(containerId);
      if(v==null)
    	  return;
      completed.incrementAndGet();
      /*if(!hasMoreContainers()){
          LOG.info("Starting next trackers" );
    	  for(ContainerTracker t : nextTrackers){
    		  try {
				t.init(factory);
    		  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    		  }
    	  }
      }*/
    }

    public void removeContainerRequests(){
    	LOG.info("Removing container requests");
    	for(ContainerRequest c : containerRequests){
        	LOG.info("Removing cores: "+c.getCapability().getVirtualCores()+" mem: "+c.getCapability().getMemory());
        	service.resourceManager.removeContainerRequest(c);
    	}
    	LOG.info("Blockers: "+service.resourceManager.getBlockers());
    }
    
    public void containerCompleted(ContainerId containerId) {
        isInitilized=false;
      LOG.info("Completed container id = " + containerId+" operator: "+params.getName());
      containers.remove(containerId);
      completed.incrementAndGet();
      
      service.parameters.workflow.setOutputsRunning(params.getName());

      if(!hasMoreContainers()){
    	  removeContainerRequests();
          LOG.info("Starting next trackers" );
    	  for(ContainerTracker t : nextTrackers){
    		  try {
				t.init(service.factory);
    		  } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    		  }
    	  }
      }
    }

    @Override
    public void onStartContainerError(ContainerId containerId, Throwable throwable) {
      LOG.warn("Start container error for container id = " + containerId, throwable);
      containers.remove(containerId);
      completed.incrementAndGet();
      failed.incrementAndGet();
    }

    @Override
    public void onGetContainerStatusError(ContainerId containerId, Throwable throwable) {
      LOG.error("Could not get status for container: " + containerId, throwable);
    }

    @Override
    public void onStopContainerError(ContainerId containerId, Throwable throwable) {
      LOG.error("Failed to stop container: " + containerId, throwable);
      completed.incrementAndGet();
    }

    public boolean needsContainers() {
        //LOG.info("operator: "+params.getName()+" needed: "+needed);
        return needed.get() > 0;
    }

    public boolean matches(Container c) {
      return containerRequests.get(0).getCapability().getVirtualCores()==c.getResource().getVirtualCores() && containerRequests.get(0).getCapability().getMemory()==c.getResource().getMemory(); 
    }

    public void launchContainer(Container c) {
      LOG.info("Launching container id = " + c.getId() + " on node = " + c.getNodeId()+" operator: "+params.getName());
      containers.put(c.getId(), c);
      needed.decrementAndGet();
		try {
			this.ctxt = service.factory.create(params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      nodeManager.startContainerAsync(c, ctxt);
    }

    public boolean hasRunningContainers() {
      return !containers.isEmpty();
    }

    public void kill() {
      for (Container c : containers.values()) {
        nodeManager.stopContainerAsync(c.getId(), c.getNodeId());
      }
    }

    public boolean hasMoreContainers() {
      return needsContainers() || hasRunningContainers();
    }
  }
