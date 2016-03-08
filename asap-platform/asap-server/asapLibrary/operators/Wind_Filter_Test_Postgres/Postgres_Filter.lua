-- The command to execute.
SCRIPT = "Postgres_Filter.sh"
SHELL_COMMAND = "./" .. SCRIPT

-- Operator's home directory
FILTER_TEST_HOME="asapLibrary/operators/Wind_Filter_Test_Postgres"

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
			["Postgres_Filter.sh"] = {
				file = FILTER_TEST_HOME .. "/" .. SCRIPT,
				type = "file",               -- other value: 'archive'
				visibility = "application",  -- other values: 'private', 'public'
			}
		},
		command = {
			base = SHELL_COMMAND
		}
	}
}
