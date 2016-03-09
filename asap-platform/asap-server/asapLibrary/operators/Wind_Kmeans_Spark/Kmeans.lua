-- The command to execute.
SHELL_COMMAND = "./Kmeans.sh"

-- Home directory of operator
KMEANS_HOME = "asapLibrary/operators/Wind_Kmeans_Spark"

-- The actual distributed shell job.
operator = yarn {
	name = "Execute Kmeans_Spark Operator",
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
    			["Kmeans.sh"] = {
				file = KMEANS_HOME .. "/Kmeans.sh",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["archetipi.csv"] = {
       				file = KMEANS_HOME .. "/archetipi.csv",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			["clustering.py"] = {
       				file = KMEANS_HOME .. "/clustering.py",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			}
  			}		
 	}
}
