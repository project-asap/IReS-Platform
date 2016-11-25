-- Specific configuration of operator
ENGINE = "Spark"
OPERATOR = "Move_Postgres_" .. ENGINE
SCRIPT = OPERATOR .. ".sh"
SHELL_COMMAND = "./" .. SCRIPT
-- Home directory of operator
OPERATOR_LIBRARY = "asapLibrary/operators"
OPERATOR_HOME = OPERATOR_LIBRARY .. "/" .. OPERATOR

-- The actual distributed shell job.
operator = yarn {
  name = "Execute " .. OPERATOR .. " Operator",
  labels = "postgres",
  nodes = "slave-1",
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    command = {
		base = SHELL_COMMAND
  	},
    resources = {
		[ SCRIPT] = {
			file = OPERATOR_HOME .. "/" .. SCRIPT,
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		},
		[ "convertCSV2Parquet.py"] = {
			file = OPERATOR_HOME .. "/convertCSV2Parquet.py",
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    }
  }
}
