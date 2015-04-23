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
package gr.ntua.ece.cslab.panic.server.shared;

import java.io.InputStream;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Giannis Giannakopoulos
 */
public class SystemLogger {
    
    private static final Logger logger = Logger.getLogger(SystemLogger.class);

    /**
     * Method used to initialize the SystemLogger. This method expects to read
     * a log4j.properties file (must be in java classpath), or uses a basic configuration
     * where any logging information are printed to the stdout.
     */
    public static void configureLogger() {
        InputStream logPropertiesStream = SystemLogger.class.getClassLoader().getResourceAsStream("log4j.properties");
        if (logPropertiesStream != null) {
            PropertyConfigurator.configure(logPropertiesStream);
        } else {
            BasicConfigurator.configure();
        }
    }
    
    /**
     * Returns the logger object.
     * @return the logger
     */
    public static Logger get() {
        return logger;
    }

}
