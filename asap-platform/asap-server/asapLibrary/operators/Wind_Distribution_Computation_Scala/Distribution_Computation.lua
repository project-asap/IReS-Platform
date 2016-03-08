-- The command to execute.
SHELL_COMMAND = "./Distribution_Computation.sh"
-- The actual distributed shell job.
operator = yarn {
	name = "Execute Wind_Distribution_Computation_Scala Operator",
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
    			["Distribution_Computation.sh"] = {
				file = DATA_FILTER_HOME  .. "/Distribution_Computation.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["DistributionCompuration.scala"] = {
       				file = DATA_FILTER_HOME .. "/DistributionCompuration.scala",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["Types.scala"] = {
				file = DATA_FILTER_HOME .. "/Types.scala",
				type = "file",
				visibility = "application"
				}
  		}
    		
 	}
}
