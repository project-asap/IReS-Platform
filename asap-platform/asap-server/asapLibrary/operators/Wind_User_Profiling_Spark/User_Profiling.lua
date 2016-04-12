-- General configuration of the operators belonging to Wind_Demo_o_Postgres workflow
BASE = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster"
TIMEOUT = -1
MEMORY = 3072
CORES = 1
OPERATOR_LIBRARY = "asapLibrary/operators"

-- Specific configuration of operator
OPERATOR = "Wind_User_Profiling_Spark"
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
    			["Wind_User_Profiling_Spark.sh"] = {
					file = OPERATOR_HOME .. "/Wind_User_Profiling_Spark.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["user_profiling.py"] = {
       				file = OPERATOR_HOME .. "/user_profiling.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["aree_roma.csv"] = {
					file = OPERATOR_HOME .. "/aree_roma.csv",
					type = "file",
					visibility = "application"
				}
  		}		
 	}
}
