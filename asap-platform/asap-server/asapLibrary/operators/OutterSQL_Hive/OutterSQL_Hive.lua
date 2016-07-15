-- Specific configuration of operator
ENGINE = "Hive"
OPERATOR = "OutterSQL_" .. ENGINE
SCRIPT = OPERATOR .. ".sh"
SHELL_COMMAND = "./" .. SCRIPT
-- Home directory of operator
OPERATOR_HOME = OPERATOR_LIBRARY .. "/" .. OPERATOR

-- The actual distributed shell job.
operator = yarn {
	name = "Execute " .. OPERATOR .. " Operator",
	container = {
    		instances = CONTAINER_INSTANCES,
    		env = base_env,
    		command = {
				base = SHELL_COMMAND
			},
    		resources = {
    			[ SCRIPT] = {
					file = OPERATOR_HOME .. "/" .. SCRIPT,
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			[ "tpchQuery17Outter" .. ENGINE .. ".sql"] {
					file = OPERATOR_HOME .. "/tpchQuery17Outter" .. ENGINE .. ".sql",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			},
    			[ "tpchQuery18Outter" .. ENGINE .. ".sql"] {
					file = OPERATOR_HOME .. "/tpchQuery18Outter" .. ENGINE .. ".sql",
      				type = "file",               -- other value: 'archive'
      				visibility = "application",  -- other values: 'private', 'public'
    			}
  			}		
 	}
}
