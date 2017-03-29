operator = yarn {
  name = "WindLatestClusteringSwan",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestClusteringSwan/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["clustering.sh"] = {
       file = "asapLibrary/operators/WindLatestClusteringSwan/clustering.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["clustering_swan.py"] = {
       file = "asapLibrary/operators/WindLatestClusteringSwan/clustering_swan.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["wind_kmeans"] = {
       file = "asapLibrary/operators/WindLatestClusteringSwan/wind_kmeans",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestClusteringSwan/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./clustering.sh"
    }
  }
}