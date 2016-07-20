-- The actual distributed shell job.
operator = yarn {
  name = "Execute Move_Hive_Postgres Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  labels = "postgres",
  nodes = "path",
  container = {
    instances = CONTAINER_INSTANCES,
    resources = {
	    ["Move_Hive_Postgres.sh"] = {
			file = "asapLibrary/operators/Move_Hive_Postgres/Move_Hive_Postgres.sh",
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
	base = "./Move_Hive_Postgres.sh"
    }
  }
}
