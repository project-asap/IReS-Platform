/*
 * Copyright 2014 Giannis Giannakopoulos.
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
package gr.ntua.ece.cslab.panic.server;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import gr.ntua.ece.cslab.panic.server.shared.SystemLogger;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

class RunServer {

    public static void main(String[] args) throws Exception {

        Properties prop = new Properties();
        InputStream resourceFile = RunServer.class.getClassLoader().getResourceAsStream("server.properties");
        if (resourceFile != null) {
            prop.load(resourceFile);
        } else {
            System.err.println("File not exist...");
            System.exit(1);
        }

        ServletHolder holder = new ServletHolder(ServletContainer.class);
        holder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        holder.setInitParameter("com.sun.jersey.config.property.packages", "gr.ntua.ece.cslab.panic.server.rest");//Set the package where the services reside
        holder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        holder.setInitOrder(1);

        Server server = new Server(new Integer(prop.getProperty("server.port")));
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet(holder, "/*");
        server.start();

        SystemLogger.configureLogger();

        SystemLogger.get().info("Server started");
        server.join();
        SystemLogger.get().info("Server joined");

    }
}
