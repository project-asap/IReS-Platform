operator = yarn {
  name = "WindLatestClusteringMllib",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestClusteringMllib/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["cdr.py"] = {
       file = "asapLibrary/operators/WindLatestClusteringMllib/cdr.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["clustering.sh"] = {
       file = "asapLibrary/operators/WindLatestClusteringMllib/clustering.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["clustering.py"] = {
       file = "asapLibrary/operators/WindLatestClusteringMllib/clustering.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestClusteringMllib/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./clustering.sh"
    }
  }
}