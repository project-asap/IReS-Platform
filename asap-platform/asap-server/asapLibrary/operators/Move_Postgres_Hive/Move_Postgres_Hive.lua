-- The actual distributed shell job.
operator = yarn {
  name = "Execute Move_Postgres_Hive Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  labels = "postgres",
  nodes = "path",
  container = {
    instances = CONTAINER_INSTANCES,
    resources = {
		["Move_Postgres_Hive.sh"] = {
			file = "asapLibrary/operators/Move_Postgres_Hive/Move_Postgres_Hive.sh",
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
		base = "./Move_Postgres_Hive.sh"
    }
  }
}
