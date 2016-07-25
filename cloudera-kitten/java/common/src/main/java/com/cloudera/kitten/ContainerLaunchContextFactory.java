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
package com.cloudera.kitten;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import com.cloudera.kitten.lua.LuaContainerLaunchParameters;
import com.cloudera.kitten.lua.LuaFields;
import com.cloudera.kitten.lua.LuaPair;
import com.cloudera.kitten.lua.LuaWrapper;

/**
 * Functions for constructing YARN objects from the parameter values.
 */
public class ContainerLaunchContextFactory {

  private final Resource clusterMax;
  
  public ContainerLaunchContextFactory(Resource clusterMax) {
    this.clusterMax = clusterMax;
  }
  
  public ContainerLaunchContext create(ContainerLaunchParameters parameters) throws IOException {
    ContainerLaunchContext clc = Records.newRecord(ContainerLaunchContext.class);
    clc.setCommands(parameters.getCommands());
    //System.out.println("Commands: "+clc.getCommands());
    //System.out.println("Environment: "+parameters.getEnvironment());
    clc.setEnvironment(parameters.getEnvironment());
    clc.setLocalResources(parameters.getLocalResources());
    return clc;
  }
  
  
  public Resource createResource(ContainerLaunchParameters parameters) {
    return parameters.getContainerResource(clusterMax);
  }
  
  public Priority createPriority(int priority) {
    Priority p = Records.newRecord(Priority.class);
    p.setPriority(priority);
    return p;
  }
  
}
