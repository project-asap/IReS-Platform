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

package gr.ntua.ece.cslab.panic.server.containers;

import gr.ntua.ece.cslab.panic.server.containers.beans.ApplicationInfo;
import gr.ntua.ece.cslab.panic.server.containers.beans.ProfilingInfo;

/**
 *
 * Class used to hold and execute all the necessary tasks to model an application.
 * @author Giannis Giannakopoulos
 */
public class Application {
    
    private ApplicationInfo appInfo;
    private ProfilingInfo profilingDetails;

    public Application() {
    
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public ProfilingInfo getProfilingDetails() {
        return profilingDetails;
    }

    public void setProfilingDetails(ProfilingInfo profilingDetails) {
        this.profilingDetails = profilingDetails;
    }
    
    
}
