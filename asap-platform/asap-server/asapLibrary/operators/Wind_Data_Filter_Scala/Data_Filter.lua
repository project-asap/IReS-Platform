-- The command to execute.
SHELL_COMMAND = "./Data_Filter.sh"
-- The actual distributed shell job.
operator = yarn {
	name = "Execute Data_Filter_Scala Operator",
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
    			["Data_Filter.sh"] = {
				file = DATA_FILTER_HOME  .. "/Data_Filter.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["DataFilter.scala"] = {
       				file = DATA_FILTER_HOME .. "/DataFilter.scala",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["DataFilterSettings.scala"] = {
				file = DATA_FILTER_HOME .. "/DataFilterSettings.scala",
				type = "file",
				visibility = "application"
    			},
    			["Types.scala"] = {
				file = DATA_FILTER_HOME .. "/Types.scala",
				type = "file",
				visibility = "application"
				}
  		}
    		
 	}
}
