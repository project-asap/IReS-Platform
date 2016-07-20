-- The actual distributed shell job.
operator = yarn {
  name = "Execute HIVE_GroupBy_Sort Operator",
  timeout = -1,
  memory = 512,
  cores = 1,
  container = {
    instances = CONTAINER_INSTANCES,
    stageout = {"output"},
    resources = {
	    ["HIVE_GroupBy_Sort.sh"] = {
       		file = "asapLibrary/operators/HIVE_GroupBy_Sort/HIVE_GroupBy_Sort.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application",  -- other values: 'private', 'public'
		}
    },
    command = {
	base = "./HIVE_GroupBy_Sort.sh"
    }
  }
}
