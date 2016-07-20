-- The actual distributed shell job.
operator = yarn {
  name = "Execute Postgres_Join_Filter Operator",
  timeout = -1,
  memory = 1024,
  cores = 1,
  labels = "postgres",
  nodes = "path",
  container = {
    instances = CONTAINER_INSTANCES,
    stageout = {"output"},
    resources = {
	    ["Postgres_Join_Filter.sh"] = {
			file = "asapLibrary/operators/Postgres_Join_Filter/Postgres_Join_Filter.sh",
			type = "file",               -- other value: 'archive'
			visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
	base = "./Postgres_Join_Filter.sh"
    }
  }
}
