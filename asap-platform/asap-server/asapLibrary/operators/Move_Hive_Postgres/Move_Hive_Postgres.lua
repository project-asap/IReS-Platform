SHELL_COMMAND = "./Move_Hive_Postgres.sh"
CONTAINER_INSTANCES = 1
MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

CP = "/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/share/hadoop/common/lib/*:/opt/hadoop-2.6.0/share/hadoop/common/*:/opt/hadoop-2.6.0/share/hadoop/hdfs:/opt/hadoop-2.6.0/share/hadoop/hdfs/lib/*:/opt/hadoop-2.6.0/share/hadoop/hdfs/*:/opt/hadoop-2.6.0/share/hadoop/yarn/lib/*:/opt/hadoop-2.6.0/share/hadoop/yarn/*:/opt/hadoop-2.6.0/share/hadoop/mapreduce/lib/*:/opt/hadoop-2.6.0/share/hadoop/mapreduce/*:/contrib/capacity-scheduler/*.jar:/opt/hadoop-2.6.0/share/hadoop/yarn/*:/opt/hadoop-2.6.0/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./Move_Hive_Postgres.sh"}, ":"),
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
    resources = {
    ["Move_Hive_Postgres.sh"] = {
       file = "/opt/npapa/asap-server/asapLibrary/operators/Move_Hive_Postgres/Move_Hive_Postgres.sh",
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
