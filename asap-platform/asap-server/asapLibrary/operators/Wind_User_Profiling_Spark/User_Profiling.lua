-- The command to execute.
SHELL_COMMAND = "./User_Profiling.sh"
-- The actual distributed shell job.
operator = yarn {
	name = "Execute User_Profiling_Spark Operator",
  	timeout = 10000000,
  	memory = 1024,
  	cores = 1,
	nodes = "master",
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
    			["user_profilo.py"] = {
       				file = USER_PROFILING_HOME .. "/user_profilo.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["centro_roma.csv"] = {
					file = USER_PROFILING_HOME .. "/centro_roma.csv",
					type = "file",
					visibility = "application"
				}
  		}		
 	}
}
