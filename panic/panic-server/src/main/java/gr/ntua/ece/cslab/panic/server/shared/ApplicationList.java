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

import gr.ntua.ece.cslab.panic.server.containers.Application;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Class used to hold the application cases and executes operations on them.
 * @author Giannis Giannakopoulos
 */
public class ApplicationList {
    
    private static final Properties prop = configureProperties();
    
    public static ConcurrentHashMap<String, Application> map = new ConcurrentHashMap<>();
    
    /**
     * Method used to add an Application to the Application list. Upon the addition
     * of the application, the unique application id which references the 
     * application is returned.
     * @param application
     * @return 
     */
    public static String add(Application application) {
        String id = getUniqueId();
        application.getAppInfo().setId(id);
        map.put(id, application);
        return id;
    }
    
    /**
     * Returns the application with the specified id.
     * @param id
     * @return 
     */
    public static Application get(String id) {
        return map.get(id);
    }
    
    public static Application remove(String id) {
        return map.remove(id);
    }
    /**
     * Returns a hash map consisting of the application keys and names.
     * @return 
     */
    public static HashMap<String, String> getShortList() {
        HashMap<String,String> hashMap = new HashMap<>();
        for(Map.Entry<String,Application> e : map.entrySet()) {
            hashMap.put(e.getKey(), e.getValue().getAppInfo().getName());
        }
        return hashMap;
    }
    
    synchronized private static String getUniqueId() {
        while (true) {
            StringBuilder builder = new StringBuilder();
            Random rand = new Random();
            for (int i = 0; i < new Integer(prop.getProperty("application.id.length", "8")); i++) {
                int v = rand.nextInt(16);
                builder.append(Integer.toHexString(v));
            }
            String id = builder.toString();
            if(!map.containsKey(id)){
                return id;
            }
        }
    }
        
    private static Properties configureProperties() {
        Properties properties = new Properties();
        InputStream resourceFile = ApplicationList.class.getClassLoader().getResourceAsStream("server.properties");
        try {
            properties.load(resourceFile);
        } catch (IOException ex) {
            SystemLogger.get().warn("No conf set", ex);
        }
        return properties;

    }
}
