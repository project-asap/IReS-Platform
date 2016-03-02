-- The command to execute.
SCRIPT = "Postgres_Join.sh"
SHELL_COMMAND = "./" .. SCRIPT
-- The number of containers to run ApplicationMaster
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "/home/hadoop/upload_asap/IReS-Platform/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"

-- definitions like YARN home folder and CLASSPATH setup
--  operator relative home directory in target folder
JOIN_TEST_HOME = "asapLibrary/operators/Wind_Join_Test_Postgres"
--  CLASSPATH setup.
-- taken from hadoop itself: HOME_YARN/bin/hadoop classpath
CP = "/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/etc/hadoop:/home/hadoop/yarn/share/hadoop/common/lib/*:/home/hadoop/yarn/share/hadoop/common/*:/home/hadoop/yarn/share/hadoop/hdfs:/home/hadoop/yarn/share/hadoop/hdfs/lib/*:/home/hadoop/yarn/share/hadoop/hdfs/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/mapreduce/lib/*:/home/hadoop/yarn/share/hadoop/mapreduce/*:/home/hadoop/yarn/contrib/capacity-scheduler/*.jar:/home/hadoop/yarn/share/hadoop/yarn/*:/home/hadoop/yarn/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", SHELL_COMMAND}, ":"),
}

-- The actual distributed shell job.
operator = yarn {
	name = "Execute Java Operator",
	timeout = -1,
	memory = 1024,
	cores = 1,
	labels = "postgres",
	nodes = "slave1",
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
		stageout = {"output"},
		resources = {
			["Postgres_Join.sh"] = {
				file = JOIN_TEST_HOME .. SHELL_COMMAND,
				type = "file",               -- other value: 'archive'
				visibility = "application",  -- other values: 'private', 'public'
		}
	},
	command = {
		base = SHELL_COMMAND
	}
  }
}
