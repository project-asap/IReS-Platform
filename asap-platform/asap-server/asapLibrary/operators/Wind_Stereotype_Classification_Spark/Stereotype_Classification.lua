-- The command to execute.
SHELL_COMMAND = "./Stereotype_Classification.sh"

-- Home directory of operator
STEREOTYPE_CLASSIFICATION_HOME = "asapLibrary/operators/Wind_Stereotype_Classification_Spark"

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
    			},
    			["stereo_type_classification.py"] = {
					file = STEREOTYPE_CLASSIFICATION_HOME .. "/stereo_type_classification.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			}    			
    		}		
 	}
}
