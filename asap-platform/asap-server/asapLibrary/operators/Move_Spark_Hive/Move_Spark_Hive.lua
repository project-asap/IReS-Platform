-- Specific configuration of operator
ENGINE = "Hive"
OPERATOR = "Move_Spark_" .. ENGINE
SCRIPT = OPERATOR .. ".sh"
SHELL_COMMAND = "./" .. SCRIPT
-- Home directory of operator
OPERATOR_HOME = OPERATOR_LIBRARY .. "/" .. OPERATOR

-- The actual distributed shell job.
operator = yarn {
  name = "Execute " .. OPERATOR .. " Operator",
  labels = "postgres",
  nodes = "path",
  container = {
    instances = 1,
    command = {
		base = SHELL_COMMAND
  	}
    resources = {
		[ SCRIPT] = {
			file = OPERATOR_HOME .. "/" SCRIPT,
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    }
  }
}
