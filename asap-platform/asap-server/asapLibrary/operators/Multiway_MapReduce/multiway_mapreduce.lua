-- The command to execute.
SHELL_COMMAND = "./multiway_mapreduce.sh"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- definitions like YARN home folder and CLASSPATH setup
--  home directory of hadoop user running YARN
HOME = "/home/hadoop"
--  operator home directory
MULTIWAY_MAPREDUCE_HOME = HOME .. "/asap/IReS-Platform/asap-server/asapLibrary/operators/Multiway_MapReduce"
-- Hadoop
--  YARN home directory in local file system
HOME_YARN = HOME .. "/yarn"
--  YARN execution home directory
HOME_YARN_HADOOP = HOME_YARN .. "/share/hadoop"
--  CLASSPATH setup.
CP= $HOME_YARN .. "/etc/hadoop:" .. HOME_YARN_HADOOP .. "/common/lib/*:" .. HOME_YARN_HADOOP .. "/common/*:" .. HOME_YARN_HADOOP .. "/hdfs:" .. HOME_YARN_HADOOP .. "/hdfs/lib/*:" .. HOME_YARN_HADOOP .. "/hdfs/*:" .. HOME_YARN_HADOOP .. "/yarn/lib/*:" .. HOME_YARN_HADOOP .. "/yarn/*:" .. HOME_YARN_HADOOP .. "/mapreduce/lib/*:" .. HOME_YARN_HADOOP .. "/mapreduce/*:/contrib/capacity-scheduler/*.jar"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./multiway_mapreduce.sh"}, ":"),
}

-- The actual distributed shell job.
operator = yarn {
  name = "Execute Multiway Join Map Reduce Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  master = {
    env = base_env,
    resources = base_resources,
    command = {
      base = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster",
      args = { "-conf job.xml" },
    }
  },

  container = {
    instances = CONTAINER_INSTANCES,
    env = base_env,
    resources = {
    ["multiway_mapreduce.sh"] = {
			file = MULTIWAY_MAPREDUCE_HOME .. "/multiway_mapreduce.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application",  -- other values: 'private', 'public'
		},
    ["spark_kmeans_text.py"] = {
       		file = MULTIWAY_MAPREDUCE_HOME .. "/experiments.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
	base = SHELL_COMMAND,
--	args = { "1> <LOG_DIR>/stdout", "2> <LOG_DIR>/stderr" },
    }
  }
}
