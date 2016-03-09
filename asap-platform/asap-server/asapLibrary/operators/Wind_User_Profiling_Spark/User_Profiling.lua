-- The command to execute.
SHELL_COMMAND = "./User_Profiling.sh"

-- Home directory of operator
USER_PROFILING_HOME = "asapLibrary/operators/Wind_User_Profiling_Spark"

-- The actual distributed shell job.
operator = yarn {
	name = "Execute User_Profiling_Spark Operator",
  	timeout = 10000000,
  	memory = 1024,
  	cores = 1,
	nodes = "hdp1.itc.unipi.it",
  	master = {
    		env = base_env,
    		resources = base_resources,
    		command = {
      			base = "${JAVA_HOME}/bin/java -Xms64m -Xmx1280m com.cloudera.kitten.appmaster.ApplicationMaster",
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
    			["User_Profiling.sh"] = {
					file = USER_PROFILING_HOME .. "/User_Profiling.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["user_profiling.py"] = {
       				file = USER_PROFILING_HOME .. "/user_profiling.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["arre_roma.csv"] = {
					file = USER_PROFILING_HOME .. "/arre_roma.csv",
					type = "file",
					visibility = "application"
				}
  		}		
 	}
}
