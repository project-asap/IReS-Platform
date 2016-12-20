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



import gr.ntua.cslab.asap.daemon.ServerStaticComponents;
import gr.ntua.cslab.asap.operators.Operator;
import gr.ntua.cslab.asap.operators.SpecTree;
import gr.ntua.cslab.asap.rest.beans.OperatorDescription;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

@Path("/operators/")
public class Operators {

    public Logger logger = Logger.getLogger(Operators.class);
    
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String listOperators() {
    	List<String> l = OperatorLibrary.getOperators();
    	String ret = "<ul>";
    	for(String op : l){
			ret+= "<li><a href=\"operators/"+op+"\">"+op+"</a></li>";
    		
    	}
    	ret+="</ul>";
        return ret;
    }

    @GET
    @Path("{id}/")
    @Produces(MediaType.TEXT_HTML)
    public String getApplicationInfo(@PathParam("id") String id) {
    	return OperatorLibrary.getOperatorDescription(id);
    }
    
    @GET
    @Path("json/{id}/")
	@Produces("application/json")
    public OperatorDescription getOperatorInfoJson(@PathParam("id") String id) {
    	return OperatorLibrary.getOperatorDescriptionJSON(id);
    }
    
    
    @GET
    @Path("XML/{id}/")
	@Produces("application/XML")
    public OperatorDescription getOperatorInfoXML(@PathParam("id") String id) {
    	return OperatorLibrary.getOperatorDescriptionJSON(id);
    }
    

    @GET
    @Path("add/")
	@Produces("application/XML")
    public String addOperator(
            @QueryParam("opname") String opname,
            @QueryParam("opString") String opString) throws Exception {
    	
    	OperatorLibrary.addOperator(opname, opString);
    	return "OK";
    }
    
    @GET
    @Path("delete/")
	@Produces("application/XML")
    public String deleteOperator(
            @QueryParam("opname") String opname) throws Exception {
    	
    	OperatorLibrary.deleteOperator(opname);
    	return "OK";
    }
    
    /**
     * Stores an operator from a tarball
     * */
    private void storeOperator(InputStream is, String outputDir) throws Exception {
    	
    	logger.info("Writting operator to: "+outputDir);
    	
    	File file = new File(outputDir);
        file.mkdir();
    	
        TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        TarArchiveEntry entry = null; 
        while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
            	logger.info(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
                if (!outputFile.exists()) {
                	logger.info(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                }
            } else {
            	logger.info(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
                final OutputStream outputFileStream = new FileOutputStream(outputFile); 
                IOUtils.copy(debInputStream, outputFileStream);
                outputFileStream.close();
            }
        }

        debInputStream.close();
    }

    /**
     * Generates the content of the .lua file
     * @param params The execution parameters. For example
     *               Execution.memory=1024
     *               Execution.cores=1
     * */
    private static String generateLua(HashMap<String, String> params){
        return String.format("operator = yarn {\n" +
                        "  name = \"%s\",\n" +
                        "  timeout = 10000,\n" +
                        "  memory = %s,\n" +
                        "  cores = %s,\n" +
                        "  container = {\n" +
                        "    instances = 1,\n" +
                        "    --env = base_env,\n" +
                        "    resources = {\n" +
                        "    %s\n" +
                        "    },\n" +
                        "    command = {\n" +
                        "        base = \"%s\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                params.get("name"),
                params.get("memory"),
                params.get("cores"),
                params.get("resources"),
                params.get("command"));
    }

    /**
     * Generates the actual .lua file
     */
    private boolean generateLua(Operator op){
        try {
            HashMap<String, String> luaParams = new HashMap<>();
            SpecTree params = op.optree;
            String name = op.opName;
            String resources = "";
            String cores = params.getParameter("Execution.cpu");
            String memory = params.getParameter("Execution.memory");
            String command = params.getParameter("Execution.command");

            //Default values
            if (cores == null) cores = "1";
            if (memory == null) memory = "1024";

            //If a command is not set in the description file
            //the first shell script found will be set as the
            //execution command
            if (command == null) {
                logger.warn("Command not found in description, searching for an executable");
                for (File file : (new File(op.directory)).listFiles()) {
                    if (file.getName().endsWith(".sh")) {
                        logger.warn("./"+file.getName()+" added as execution command");
                        command = "./" + file.getName();
                        break;
                    }
                }
            }

            //Add all the files in the folder as resources
            File[] files = (new File(op.directory)).listFiles();
            int fileCounter = 0;
            for (File file : files) {
                ++fileCounter;
                resources += genResourceFromFile(file);
                if (fileCounter != files.length) resources += ",\n\t  ";
            }

            luaParams.put("name", name);
            luaParams.put("cores", cores);
            luaParams.put("memory", memory);
            luaParams.put("command", command);
            luaParams.put("resources", resources);

            FileWriter fw = new FileWriter(String.format("%s/%s.lua", op.directory, op.opName));
            fw.write(generateLua(luaParams));
            fw.close();

            op.optree.add("Execution.LuaScript", op.opName + ".lua");
            logger.info(op.opName+".lua written successfully");
        }
        catch (IOException ioException) {
            logger.warn("IO Exception");
        }

        return true;
    }

    /**
     * Generates a container resource in json format from a file
     * */
    private String genResourceFromFile(File file){
        return String.format("[\"%s\"] = {\n" +
                "       file = \"%s\",\n" +
                "                type = \"file\",               -- other value: 'archive'\n" +
                "                visibility = \"application\"  -- other values: 'private', 'public'\n" +
                "        }",file.getName(), file.getPath());
    }

    /**
     * Checks if the operator contains a .lua file
     * */
    private boolean containsLua(String operatorPath){
        File f = new File(operatorPath);
        if (f.isDirectory()){
            File[] files = f.listFiles();
            for (File file : files){
                if (file.getName().endsWith(".lua")){
                    return true;
                }
            }
        }
        else{
            logger.warn("Invalid path");
        }

        return false;
    }

    /**
     * Validates an operator by checking if all the
     * required files exist.
     * */
    private boolean isValid(File operatorFolder) {
        String[] files = operatorFolder.list();
        boolean description = Arrays.asList(files).contains("description");

        return description;
    }
    
    @POST
    @Path("addTarball/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces("application/XML")
    public String addOperator(@QueryParam("opname") String opname, @Context HttpServletRequest request, InputStream input) throws Exception {
    	String folder = OperatorLibrary.operatorDirectory+"/"+opname;
        storeOperator(input, folder);

        if (!isValid(new File(folder))) {
            return "description file not found!";
        }

        Operator op = new Operator(opname, folder);
        op.readFromDir();
        if (!containsLua(folder)){
            //No .lua file found, generate it
            logger.info(".lua not found, generating it...");
            generateLua(op);
        }
        OperatorLibrary.add(op);
    	return "OK";
    }

}