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


package gr.ntua.cslab.asap.daemon;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import gr.ntua.cslab.asap.staticLibraries.AbstractOperatorLibrary;
import gr.ntua.cslab.asap.staticLibraries.ClusterStatusLibrary;
import gr.ntua.cslab.asap.staticLibraries.DatasetLibrary;
import gr.ntua.cslab.asap.staticLibraries.MaterializedWorkflowLibrary;
import gr.ntua.cslab.asap.staticLibraries.OperatorLibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Runnable;
import java.lang.Thread;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.sun.jersey.api.core.ResourceConfig;
import com.palominolabs.jersey.cors.CorsResourceFilterFactory;

public class Main {

    private static void loadProperties() throws IOException {
        InputStream stream = Main.class.getClassLoader().getResourceAsStream("asap.properties");
        if (stream == null) {
            System.err.println("No asap.properties file was found! Exiting...");
            System.exit(1);
        }
        ServerStaticComponents.properties = new Properties();
        ServerStaticComponents.properties.load(stream);
        
    }

    private static void configureServer() throws Exception {
        ServerStaticComponents.server = new Server();
        int plainPort = -1, sslPort = -1;
        String keystorePath = null, keystorePassword = null;
        ServerConnector connector = null, sslConnector = null;

        if (ServerStaticComponents.properties.getProperty("server.plain.port") != null) {
            plainPort = new Integer(ServerStaticComponents.properties.getProperty("server.plain.port"));
        }
        if (ServerStaticComponents.properties.getProperty("server.ssl.port") != null) {
            sslPort = new Integer(ServerStaticComponents.properties.getProperty("server.ssl.port"));
            keystorePath = ServerStaticComponents.properties.getProperty("server.ssl.keystore.path");
            keystorePassword = ServerStaticComponents.properties.getProperty("server.ssl.keystore.password");
        }

        if (plainPort != -1) {
            connector = new ServerConnector(ServerStaticComponents.server);
            connector.setPort(plainPort);
        }

        if (sslPort != -1) {
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keystorePath);
            sslContextFactory.setKeyStorePassword(keystorePassword);
            sslContextFactory.setKeyManagerPassword(keystorePassword);

            sslConnector = new ServerConnector(
                    ServerStaticComponents.server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https));
            sslConnector.setPort(sslPort);

        }
        if (sslConnector != null && connector != null) {
            ServerStaticComponents.server.setConnectors(new Connector[]{connector, sslConnector});
        } else if (connector != null) {
            ServerStaticComponents.server.setConnectors(new Connector[]{connector});
        } else if (sslConnector != null) {
            ServerStaticComponents.server.setConnectors(new Connector[]{sslConnector});
        } else {
            System.err.println("Please choose one of the plain and SSL connections!");
            System.exit(1);
        }

        ServletHolder holder = new ServletHolder(ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        holder.setInitParameter("com.sun.jersey.config.property.packages",
                "gr.ntua.cslab.asap.daemon.rest;"
                + "org.codehaus.jackson.jaxrs");//Set the package where the services reside
        holder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        holder.setInitParameter("com.sun.jersey.config.feature.Formatted", "true");
        holder.setInitParameter(
                ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                CorsResourceFilterFactory.class.getCanonicalName()
        );
//
        holder.setInitOrder(1);
//
//        ServerStaticComponents.server = new Server();
        ServletContextHandler context = new ServletContextHandler(ServerStaticComponents.server, "/", ServletContextHandler.SESSIONS);
        context.addServlet(holder, "/*");
        
        //FastCGIProxyServlet cgi = new FastCGIProxyServlet();
        //ServletHolder holder2 = new ServletHolder(cgi);
        //context.addServlet(holder2, "/*.php");
        
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[] { "test.html" });
        resource_handler.setResourceBase("www/");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, context });
        ServerStaticComponents.server.setHandler(handlers);
        
        Logger.getLogger(Main.class.getName()).info("Server configured");

    }

    private static void configureLogger() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        InputStream logPropertiesStream = Main.class.getClassLoader().getResourceAsStream("log4j.properties");
        PropertyConfigurator.configure(logPropertiesStream);
        Logger.getLogger(Main.class.getName()).info("Logger configured");
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.getLogger(Main.class.getName()).info("Server is shutting down");
                    ServerStaticComponents.server.stop();
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));

    }

    private static void creatDirs() {
    	String folder = ServerStaticComponents.properties.getProperty("asap.dir");
    	
        File asapDir = new File(folder);
        if (!asapDir.exists()) {
        	asapDir.mkdir();
        }
        File abstractOpDir = new File(folder+"/abstractOperators");
        if (!abstractOpDir.exists()) {
        	abstractOpDir.mkdir();
        }
        File opDir = new File(folder+"/operators");
        if (!opDir.exists()) {
        	opDir.mkdir();
        }
        File workflowDir = new File(folder+"/workflows");
        if (!workflowDir.exists()) {
        	workflowDir.mkdir();
        }
        File abstractworkflowDir = new File(folder+"/abstractWorkflows");
        if (!abstractworkflowDir.exists()) {
        	abstractworkflowDir.mkdir();
        }     
    }

	private static void load() throws Exception {
		DatasetLibrary.initialize(ServerStaticComponents.properties.getProperty("asap.dir")+"/datasets");
		AbstractOperatorLibrary.initialize(ServerStaticComponents.properties.getProperty("asap.dir")+"/abstractOperators");
		OperatorLibrary.initialize(ServerStaticComponents.properties.getProperty("asap.dir")+"/operators");
		
		AbstractWorkflowLibrary.initialize(ServerStaticComponents.properties.getProperty("asap.dir")+"/abstractWorkflows");
		MaterializedWorkflowLibrary.initialize(ServerStaticComponents.properties.getProperty("asap.dir")+"/workflows");
		RunningWorkflowLibrary.initialize();
		ClusterStatusLibrary.initialize();
	}
	
	private static void checkServicesStatus() throws Exception{
		ClusterNodes cns = new ClusterNodes( 5000);
		Thread check = new Thread( cns);
		try{
			check.start();		
		}
		catch( Exception e){
			check.stop();
			System.err.println( "Checking for cluster services status has been disable due to an exception!");
			System.err.println( "Check the logs and the stderr for more info.");
		}
	}
	
    public static void main(String[] args) throws Exception {
        configureLogger();
        loadProperties();
        creatDirs();
        addShutdownHook();
        configureServer();
        load();

        ServerStaticComponents.server.start();
        Logger.getLogger(Main.class.getName()).info("Server is started");

        checkServicesStatus();
    }

}
