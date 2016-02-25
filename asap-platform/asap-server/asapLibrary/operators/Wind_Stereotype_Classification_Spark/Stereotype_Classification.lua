-- The command to execute.
SHELL_COMMAND = "./Stereotype_Classification.sh"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "/home/hadoop/asap/IReS-Platform/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"
--MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- definitions like YARN home folder and CLASSPATH setup
--  home directory of hadoop user running YARN
HOME = "/home/hadoop"
--  operator home directory in target folder
STEREOTYPE_CLASSIFICATION_HOME = HOME .. "/asap/IReS-Platform/asap-platform/asap-server/target/asapLibrary/operators/Wind_Stereotype_Classification_Spark"
--  CLASSPATH setup.
-- taken from hadoop itself: HOME_YARN/bin/hadoop classpath
CP = "/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/share/hadoop/common/lib/*:/home/hadoop/yarn/share/hadoop/common/*:/home/hadoop/yarn/share/hadoop/hdfs:/home/hadoop/yarn/share/hadoop/hdfs/lib/*:/home/hadoop/yarn/share/hadoop/hdfs/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/mapreduce/lib/*:/home/hadoop/yarn/share/hadoop/mapreduce/*:/home/hadoop/yarn/contrib/capacity-scheduler/*.jar:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
	
	CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./Stereotype_Classification.sh"}, ":"),
}

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
