BASE = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster"
TIMEOUT = 1000000000
MEMORY = 1024
CORES = 1
EXECUTION_NODE_LOCATION = "hdp1"
OPERATOR_LIBRARY = "asapLibrary/operators"

-- Specific configuration of operator
OPERATOR = "WebLyzard_Uploader_Spark"
SCRIPT = OPERATOR .. ".sh"
SHELL_COMMAND = "./" .. SCRIPT
-- Home directory of operator
OPERATOR_HOME = OPERATOR_LIBRARY .. "/" .. OPERATOR

-- The actual distributed shell job.
operator = yarn {
  name 		= "Execute " .. OPERATOR .. " Operator",
  timeout 	= TIMEOUT,
  memory 	= MEMORY,
  cores 	= CORES,
  nodes 	= EXECUTION_NODE_LOCATION,
  master 	= {
    env = base_env,
    resources = base_resources,
    command = {
      base = BASE,
      args = { "-conf job.xml" },
    }
  },
	
	container = {
    		instances = CONTAINER_INSTANCES,
    		env = base_env,
    		command = {
				base = SHELL_COMMAND
			},
    		resources = {
    			["WebLyzard_Uploader_Spark.sh"] = {
					file = OPERATOR_HOME .. "/" .. SCRIPT,
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["visualization/upload.py"] = {
					file = OPERATOR_HOME .. "/visualization/upload.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    		}		
 	}
}
