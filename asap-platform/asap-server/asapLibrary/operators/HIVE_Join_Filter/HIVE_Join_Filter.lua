-- The actual distributed shell job.
operator = yarn {
  name = "Execute HIVE_Join_Filter Operator",
  timeout = -1,
  memory = 512,
  cores = 1,
  container = {
    instances = CONTAINER_INSTANCES,
    resources = {
    	["HIVE_Join_Filter.sh"] = {
       		file = "asapLibrary/operators/HIVE_Join_Filter/HIVE_Join_Filter.sh",
      		type = "file",               -- other value: 'archive'
     		visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
		base = "./HIVE_Join_Filter.sh"
    }
  }
}
