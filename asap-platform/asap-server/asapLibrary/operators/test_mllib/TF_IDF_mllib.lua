-- The command to execute.
SHELL_COMMAND = "./tfidf_mllib.sh"
-- The number of containers to run it on.
CONTAINER_INSTANCES = 1
-- The location of the jar file containing kitten's default ApplicationMaster
-- implementation.
MASTER_JAR_LOCATION = "kitten-master-0.2.0-jar-with-dependencies.jar"

-- CLASSPATH setup.

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat( {"./tfidf_mllib.sh", "./spark_tfidf.py"}, ":"),
}

-- The actual distributed shell job.
operator = yarn {
  name = "Execute Java Operator",
  timeout = -1,
  memory = 512,
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
    ["tfidf_mllib.sh"] = {
       file = "/opt/npapa/asap-server/asapLibrary/operators/TF_IDF_mllib/tfidf_mllib.sh",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
	},
    ["spark_tfidf.py"] = {
       file = "/opt/npapa/asap-server/asapLibrary/operators/TF_IDF_mllib/spark_tfidf.py",
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
