-- The command to execute.
SHELL_COMMAND = "./Kmeans.sh"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "/home/hadoop/upload_asap/IReS-Platform/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"
--MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- definitions like YARN home folder and CLASSPATH setup
--  operator relative home directory in target folder
KMEANS_HOME = "asapLibrary/operators/Wind_Kmeans_Spark"
--  CLASSPATH setup.
-- taken from hadoop itself: HOME_YARN/bin/hadoop classpath
CP = "/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/share/hadoop/common/lib/*:/home/hadoop/yarn/share/hadoop/common/*:/home/hadoop/yarn/share/hadoop/hdfs:/home/hadoop/yarn/share/hadoop/hdfs/lib/*:/home/hadoop/yarn/share/hadoop/hdfs/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/mapreduce/lib/*:/home/hadoop/yarn/share/hadoop/mapreduce/*:/home/hadoop/yarn/contrib/capacity-scheduler/*.jar:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
	
	CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./Kmeans.sh"}, ":"),
}

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
    			}
  			}		
 	}
}
