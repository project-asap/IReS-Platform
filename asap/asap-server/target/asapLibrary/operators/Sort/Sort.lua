-- An example Kitten configuration file for the distributed shell app, running on
-- CDH4.
--
-- To execute, run:
--
-- hadoop jar kitten-client-0.2.0-jar-with-dependencies.jar distshell.lua distshell
--
-- from a directory that contains the client jar, the master jar, and distshell.lua.
--
-- You can also play with the SHELL_COMMAND, CONTAINER_INSTANCES, and
-- MASTER_JAR_LOCATION arguments as need be.

-- The command to execute.
SHELL_COMMAND = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m gr.ntua.operatorTest.Operator"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- CLASSPATH setup.
CP = "/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/etc/hadoop:/opt/hadoop-2.6.0/share/hadoop/common/lib/*:/opt/hadoop-2.6.0/share/hadoop/common/*:/opt/hadoop-2.6.0/share/hadoop/hdfs:/opt/hadoop-2.6.0/share/hadoop/hdfs/lib/*:/opt/hadoop-2.6.0/share/hadoop/hdfs/*:/opt/hadoop-2.6.0/share/hadoop/yarn/lib/*:/opt/hadoop-2.6.0/share/hadoop/yarn/*:/opt/hadoop-2.6.0/share/hadoop/mapreduce/lib/*:/opt/hadoop-2.6.0/share/hadoop/mapreduce/*:/contrib/capacity-scheduler/*.jar:/opt/hadoop-2.6.0/share/hadoop/yarn/*:/opt/hadoop-2.6.0/share/hadoop/yarn/lib/*"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar", "./test.jar"}, ":"),
}

-- The actual distributed shell job.
Sort = yarn {
  name = "Execute Java Operator",
  timeout = -1,
  memory = 512,

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
    ["test.jar"] = {
       file = "/opt/npapa/asapWorkflow/test.jar",
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
