/**
 * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.kitten.lua;

import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.rest.beans.OperatorDictionary;
import gr.ntua.cslab.asap.rest.beans.WorkflowDictionary;
import gr.ntua.cslab.asap.workflow.MaterializedWorkflow1;
import gr.ntua.cslab.asap.workflow.WorkflowNode;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.luaj.vm2.LuaValue;

import com.cloudera.kitten.ContainerLaunchParameters;
import com.cloudera.kitten.util.Extras;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AsapLuaContainerLaunchParameters implements ContainerLaunchParameters {

  private static final Log LOG = LogFactory.getLog(AsapLuaContainerLaunchParameters.class);
  
  private final LuaWrapper lv;
  public final Configuration conf;
  public final Map<String, URI> localFileUris;
  private final Extras extras;

	private String dir;
	private String name;
	private String execScript;
	private int globalContainerId;
	
	private MaterializedWorkflow1 workflow;
	
	private OperatorDictionary operatorDictionary;
	private WorkflowNode operator;
	
	private String opName;
  
  public AsapLuaContainerLaunchParameters(LuaValue lv, String name, Configuration conf, Map<String, URI> localFileUris, MaterializedWorkflow1 workflow, String opName) throws IOException {
    this(new LuaWrapper(lv.checktable()), name, conf, localFileUris, new Extras(),workflow, opName);
  }
  
  public AsapLuaContainerLaunchParameters(LuaWrapper lv, String name, Configuration conf, Map<String, URI> localFileUris, MaterializedWorkflow1 workflow, String opName) throws IOException {
    this(lv, name, conf, localFileUris, new Extras(),workflow, opName);
  }
  
  public AsapLuaContainerLaunchParameters(LuaWrapper lv, String name, Configuration conf,
      Map<String, URI> localFileUris, Extras extras, MaterializedWorkflow1 workflow, String opName) throws IOException {
	  this.name=name;
    this.lv = lv;
    this.conf = conf;
    this.localFileUris = localFileUris;
    this.extras = extras;
    this.workflow = workflow;
    this.opName = opName;
    this.operator = workflow.nodes.get(opName);
    
    globalContainerId=0;
  }


  public List<String> getStageOutFiles() {
      List<String> ret = new ArrayList<String>();
	    if (!lv.isNil(LuaFields.STAGEOUT)) {
	      LuaWrapper a = lv.getTable(LuaFields.STAGEOUT);
	      Iterator<LuaPair> restIter = a.arrayIterator();
	      while (restIter.hasNext()) {
	        ret.add(restIter.next().value.tojstring());
	      }
	    }
	    List<String> outputFiles= operator.getOutputFiles();
	    LOG.info("Output files: "+outputFiles);
	    ret.addAll(outputFiles);
	    return ret;
  }
  

 /* private List<String> getStageInFiles() {
      List<String> ret = new ArrayList<String>();
      for(WorkflowNode in : operator.inputs){
    	  String path = in.dataset.getParameter("Execution.path");
      }
	  ret.add(operator.operator.getParameter("Execution.Output0.fileName"));
	  return ret;
  	
  }*/
  
  public int getCores() {
    return lv.getInteger(LuaFields.CORES);
  }

  public int getMemory() {
    return lv.getInteger(LuaFields.MEMORY);
  }

  @Override
  public Resource getContainerResource(Resource clusterMax) {
	LOG.info("Max cores: "+clusterMax.getVirtualCores());
	LOG.info("Max memory: "+clusterMax.getMemory());
    Resource rsrc = Records.newRecord(Resource.class);
    rsrc.setMemory(Math.min(clusterMax.getMemory(), getMemory()));
	LOG.info("Setting cores: "+getCores());
    rsrc.setVirtualCores(Math.min(clusterMax.getVirtualCores(), getCores()));
    return rsrc;
  }

  @Override
  public int getPriority() {
    return lv.isNil(LuaFields.PRIORITY) ? 0 : lv.getInteger(LuaFields.PRIORITY);
  }

  @Override
  public int getNumInstances() {
    return lv.isNil(LuaFields.INSTANCES) ? 1 : lv.getInteger(LuaFields.INSTANCES);
  }
  
  @Override
  public Map<String, LocalResource> getLocalResources() throws IOException {
	  //System.out.println("localFileUris: "+localFileUris);
    Map<String, LocalResource> localResources = Maps.newHashMap();  
    if (!lv.isNil(LuaFields.RESOURCES)) {
      LuaWrapper lr = lv.getTable(LuaFields.RESOURCES);
      for (LuaPair lp : lr) {
        try {
          NamedLocalResource nlr = constructResource(lp);
          localResources.put(nlr.name, nlr.resource);
        } catch (IOException e) {
          LOG.error("Error constructing local resource: " + lp.key, e);
        }
      }
    }
    for (Map.Entry<String, String> elr : extras.getResources().entrySet()) {
      LocalResource rsrc = constructExtraResource(elr.getValue());
      if (rsrc != null) {
        localResources.put(elr.getKey(), rsrc);
      }
    }
    
    // Get a local resource for the configuration object.
    LocalResource confRsrc = constructExtraResource(LuaFields.KITTEN_JOB_XML_FILE);
    if (confRsrc != null) {
      localResources.put(LuaFields.KITTEN_JOB_XML_FILE, confRsrc);
    }
    

    
    addScript(localResources);
    addOperatorInputs(localResources);
    //LOG.info("localFileUris: "+localFileUris);
    LOG.info("localResources: "+localResources.keySet());
    //System.out.println(localResources);
    
    
    return localResources;
  }


  private void addOperatorInputs(Map<String, LocalResource> localResources) throws IOException {
	  LOG.info("Inputs: "+operator.getInputFiles());
	  FileSystem fs = FileSystem.get(conf);
	  for(Entry<String, String> e : operator.getInputFiles().entrySet()){
		  	if((!e.getValue().startsWith("hdfs://"))&&(!e.getValue().startsWith("$HDFS"))){
		  		LOG.info("adding local resource: "+e);
			  	String inDir =dir;
				LocalResource rsrc = Records.newRecord(LocalResource.class);
				rsrc.setType(LocalResourceType.FILE);
				rsrc.setVisibility(LocalResourceVisibility.APPLICATION);
				LOG.info("Adding input: "+inDir+"/"+e.getValue());
				Path dst = new Path(inDir+"/"+e.getValue());
				dst = fs.makeQualified(dst);
				FileStatus stat = fs.getFileStatus(dst);
				rsrc.setSize(stat.getLen());
				rsrc.setTimestamp(stat.getModificationTime());
				rsrc.setResource(ConverterUtils.getYarnUrlFromPath(dst));
				localResources.put(e.getKey(), rsrc);
		  	}
	  }
	  /*for(String in : operator.getArguments().split(" ")){
		  LOG.info("Adding input: "+in);
		  LocalResource nl = constructScriptResource();
		  localResources.put(in, nl);
	  }*/
  }

private void addScript(Map<String, LocalResource> lres) throws IOException {
	  LocalResource nl = constructScriptResource();
	  lres.put(execScript, nl);
  }

  private LocalResource constructScriptResource() throws IOException {
	    LocalResource rsrc = Records.newRecord(LocalResource.class);
	    rsrc.setType(LocalResourceType.FILE);
	    rsrc.setVisibility(LocalResourceVisibility.APPLICATION);
	    String src = "file:///tmp/"+execScript;
	    Path path = new Path(src);
	    
	    configureLocalScriptResourceForPath(rsrc, path);
	    return rsrc;
	  }

  private void configureLocalScriptResourceForPath(LocalResource rsrc, Path path) throws IOException {
	    //System.out.println("URI: "+path.toUri());
	    FileSystem fs = FileSystem.get(conf);
	    
	    Path dst = new Path(dir+"/"+path.getName());
	    fs.moveFromLocalFile(path, dst);
	    dst = fs.makeQualified(dst);
	    
	    FileStatus stat = fs.getFileStatus(dst);
	    rsrc.setSize(stat.getLen());
	    rsrc.setTimestamp(stat.getModificationTime());
	    rsrc.setResource(ConverterUtils.getYarnUrlFromPath(dst));
  }
  
  private LocalResource constructExtraResource(String key) {
    LocalResource rsrc = Records.newRecord(LocalResource.class);
    rsrc.setType(LocalResourceType.FILE);
    rsrc.setVisibility(LocalResourceVisibility.APPLICATION);
    try {
      Path path = new Path(localFileUris.get(key));
      configureLocalResourceForPath(rsrc, path);
    } catch (IOException e) {
      LOG.error("Error constructing extra local resource: " + key, e);
      return null;
    }
    return rsrc;
  }
  
  private static class NamedLocalResource {
    public final String name;
    public final LocalResource resource;
    
    public NamedLocalResource(String name, LocalResource resource) {
      this.name = name;
      this.resource = resource;
    }
  }
  
  private NamedLocalResource constructResource(LuaPair lp) throws IOException {
    LocalResource rsrc = Records.newRecord(LocalResource.class);
    LuaWrapper value = new LuaWrapper(lp.value.checktable());
    String name = lp.key.isint() ? "" : lp.key.tojstring();
    if (value.isNil(LuaFields.LOCAL_RESOURCE_TYPE)) {
      rsrc.setType(LocalResourceType.FILE);
    } else {
      rsrc.setType(LocalResourceType.valueOf(
          value.getString(LuaFields.LOCAL_RESOURCE_TYPE).toUpperCase()));
    }
    if (value.isNil(LuaFields.LOCAL_RESOURCE_VISIBILITY)) {
      rsrc.setVisibility(LocalResourceVisibility.APPLICATION);
    } else {
      rsrc.setVisibility(LocalResourceVisibility.valueOf(
          value.getString(LuaFields.LOCAL_RESOURCE_VISIBILITY).toUpperCase()));
    }
    if (!value.isNil(LuaFields.LOCAL_RESOURCE_URL)) {
      URI uri = URI.create(value.getString(LuaFields.LOCAL_RESOURCE_URL));
      rsrc.setResource(ConverterUtils.getYarnUrlFromURI(uri));
      if (name.isEmpty()) {
        name = (new File(uri.getPath())).getName();
      }
    } else if (!value.isNil(LuaFields.LOCAL_RESOURCE_HDFS_FILE)) {
      Path path = new Path(value.getString(LuaFields.LOCAL_RESOURCE_HDFS_FILE));
      configureLocalResourceForPath(rsrc, path);
      if (name.isEmpty()) {
        name = path.getName();
      }
    } else if (!value.isNil(LuaFields.LOCAL_RESOURCE_LOCAL_FILE)) {
      String src = value.getString(LuaFields.LOCAL_RESOURCE_LOCAL_FILE);
      Path path = new Path(localFileUris.get(src));
      configureLocalResourceForPath(rsrc, path);
      if (name.isEmpty()) {
        name = new Path(src).getName();
      }
    } else {
      throw new IllegalArgumentException(
          "Invalid resource: no 'url', 'hdfs', or 'file' fields specified.");
    }
    return new NamedLocalResource(name, rsrc);
  }
  
  private void configureLocalResourceForPath(LocalResource rsrc, Path path) throws IOException {
    FileSystem fs = FileSystem.get(conf);
    FileStatus stat = fs.getFileStatus(path);
    rsrc.setSize(stat.getLen());
    rsrc.setTimestamp(stat.getModificationTime());
    rsrc.setResource(ConverterUtils.getYarnUrlFromPath(path));
  }
  
  @Override
  public Map<String, String> getEnvironment() {
    Map<String, String> env = Maps.newHashMap(extras.getEnv());
    if (!lv.isNil(LuaFields.ENV)) {
      env.putAll(lv.getTable(LuaFields.ENV).asMap());
    }
    return env;
  }

  @Override
  public List<String> getCommands() throws IOException {
    List<String> cmds = Lists.newArrayList();
    if (!lv.isNil(LuaFields.COMMANDS)) {
      Iterator<LuaPair> pairsIter = lv.getTable(LuaFields.COMMANDS).arrayIterator();
      while (pairsIter.hasNext()) {
        LuaValue c = pairsIter.next().value;
        if (c.isstring()) {
          cmds.add(c.tojstring());
        } else if (c.istable()) {
          cmds.add(toCommand(new LuaWrapper(c.checktable())));
        }
      }
    } else if (!lv.isNil(LuaFields.COMMAND)) {
      if (lv.isTable(LuaFields.COMMAND)) {
        cmds.add(toCommand(lv.getTable(LuaFields.COMMAND)));
      } else {
        cmds.add(lv.getString(LuaFields.COMMAND));
      }
    }
    if (cmds.isEmpty()) {
      LOG.fatal("No commands found in container!");
    }
    

    dir = localFileUris.get(LuaFields.KITTEN_JOB_XML_FILE).getPath();
    dir = dir.substring(0, dir.lastIndexOf("/"));
    //System.out.println("Dir: " +dir);
    //String args = opName+" "+operator.getArguments();
    String args = operator.getArguments();
    
    List<String> oldcmds = cmds;
    cmds = new ArrayList<String>();
    String outdir = dir+"/"+this.name;//+"_"+globalContainerId;

	LOG.info("Inputs: "+operator.getInputFiles());
	for(Entry<String, String> e : operator.getInputFiles().entrySet()){
  		String inPath = e.getValue().replace("$HDFS_DIR", dir);
  		inPath = inPath.replace("$HDFS_OP_DIR", outdir);
  		LOG.info("adding hdfs input: "+e);
	    cmds.add("/opt/hadoop-2.6.0/bin/hadoop fs -copyToLocal "+inPath+" .");
		
	}
    cmds.add("/opt/hadoop-2.6.0/bin/hadoop fs -mkdir "+outdir);
    args = args.replace("$HDFS_DIR", dir);
    args = args.replace("$HDFS_OP_DIR", outdir);
    for(String c : oldcmds){
    	cmds.add(c+" "+args);
    }

    //List<String> stageInFiles = getStageInFiles();
    
    //System.out.println("stageOutFiles: "+stageOutFiles);
    cmds.add("ls -ltr");
    //cmds.add("ls -ltr asapData/");
    
    List<String> stageOutFiles = getStageOutFiles();
    for(String f : stageOutFiles){
	    cmds.add("/opt/hadoop-2.6.0/bin/hadoop fs -moveFromLocal "+f+" "+outdir);
    }
    //System.out.println("Container commands: "+cmds);
    execScript = writeExecutionScript(cmds);
    globalContainerId++;

	LOG.info("Commands: "+cmds);
	
    cmds = new ArrayList<String>();
    cmds.add("./"+execScript+" 1> <LOG_DIR>/stdout 2> <LOG_DIR>/stderr");
    
    return cmds;
  }
  


private String writeExecutionScript(List<String> cmds) throws IOException {
	  UUID id = UUID.randomUUID();
	  String ret = "script_"+id+".sh";
	  File fout = new File("/tmp/"+ret);
	  FileOutputStream fos = new FileOutputStream(fout);
	 
	  BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

	  bw.write("#!/bin/bash");
	  bw.newLine();
	  for(String c : cmds){
		  bw.write(c);
		  bw.newLine();
	  }
	  bw.close();
	  fout.setExecutable(true);
	  return ret;
  }
  
  public String toCommand(LuaWrapper table) {
    StringBuilder sb = new StringBuilder(table.getString(LuaFields.COMMAND_BASE));
    if (!table.isNil(LuaFields.ARGS)) {
      LuaWrapper a = table.getTable(LuaFields.ARGS);
      Iterator<LuaPair> namedArgsIter = a.hashIterator();
      while (namedArgsIter.hasNext()) {
        LuaPair lp = namedArgsIter.next();
        sb.append(" ");
        sb.append(lp.key.tojstring());
        sb.append("=");
        sb.append(lp.value.tojstring());
      }
      Iterator<LuaPair> restIter = a.arrayIterator();
      while (restIter.hasNext()) {
        sb.append(" ");
        sb.append(restIter.next().value.tojstring());
      }
    }
    return sb.toString();
  }

	@Override
	public String getName() {
		return name;
	}
}
