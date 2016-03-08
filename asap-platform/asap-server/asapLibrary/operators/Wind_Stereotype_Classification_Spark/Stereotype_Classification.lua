-- The command to execute.
SHELL_COMMAND = "./Stereotype_Classification.sh"
-- The actual distributed shell job.
operator = yarn {
	name = "Execute Stereotype_Classification_Spark Operator",
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
    			["Stereotype_Classification.sh"] = {
					file = STEREOTYPE_CLASSIFICATION_HOME .. "/Stereotype_Classification.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			}
    		}		
 	}
}
