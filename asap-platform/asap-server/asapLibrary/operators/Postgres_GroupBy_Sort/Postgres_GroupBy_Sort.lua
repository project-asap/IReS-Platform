-- The actual distributed shell job.
operator = yarn {
  name = "Execute Postgres_GroupBy_Sort Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  labels = "postgres",
  nodes = "path",
  container = {
    instances = CONTAINER_INSTANCES,
    resources = {
		["Postgres_GroupBy_Sort.sh"] = {
			file = "asapLibrary/operators/Postgres_GroupBy_Sort/Postgres_GroupBy_Sort.sh",
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
		base = "./Postgres_GroupBy_Sort.sh"
    }
  }
}
