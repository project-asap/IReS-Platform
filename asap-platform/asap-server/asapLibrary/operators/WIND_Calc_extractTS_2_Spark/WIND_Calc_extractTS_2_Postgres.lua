-- General configuration of the operators belonging to Wind_Demo_o_Postgres workflow
BASE = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster"
TIMEOUT = -1
MEMORY = 1024
CORES = 1
LABELS = "postgres"
EXECUTION_NODE_LOCATION = "hdp1"

-- Specific configuration of operator
OPERATOR = "WIND_Calc_extractTS_2_Postgres"
SCRIPT = OPERATOR .. ".sh"
SHELL_COMMAND = "./" .. SCRIPT
-- The actual distributed shell job.
operator = yarn {
  name 		= "Execute " .. OPERATOR .. " Operator",
  timeout 	= TIMEOUT,
  memory 	= MEMORY,
  cores 	= CORES,
  labels 	= LABELS,
  nodes 	= EXECUTION_NODE_LOCATION,
  master 	= {
    env = base_env,
    resources = base_resources,
    command = {
      base = BASE,
      args = { "-conf job.xml" },
    }
  },
  
  container = {
    instances = CONTAINER_INSTANCES,
    env = base_env,
    stageout = {"output"},
    resources = {
    ["WIND_Calc_extractTS_2_Postgres.sh"] = {
       file = "asapLibrary/operators/WIND_Calc_extractTS_2_Postgres/WIND_Calc_extractTS_2_Postgres.sh",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
  }
    },
    command = {
  base = SHELL_COMMAND
    }
  }
}
