SHELL_COMMAND = "./WIND_Calc_training_avgNUM_Postgres.sh"
-- The actual distributed shell job.
operator = yarn {
  name = "Execute Java Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  labels = "postgres",
  nodes = "hdp1.itc.unipi.it",
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
    ["WIND_Calc_training_avgNUM_Postgres.sh"] = {
       file = "asapLibrary/operators/WIND_Calc_training_avgNUM_Postgres/WIND_Calc_training_avgNUM_Postgres.sh",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
  }
    },
    command = {
  base = SHELL_COMMAND
    }
  }
}
