-- The command to execute.
SHELL_COMMAND = "./Peak_Detection.sh"
-- The actual distributed shell job.
operator = yarn {
	name = "Execute Peak_Detection Operator",
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
    			["Peak_Detection.sh"] = {
				file = PEAK_DETECTION_HOME  .. "/Peak_Detection.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["PeakDetection.scala"] = {
       				file = PEAK_DETECTION_HOME .. "/PeakDetection.scala",
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
