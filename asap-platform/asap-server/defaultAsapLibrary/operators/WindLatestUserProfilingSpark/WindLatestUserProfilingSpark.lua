operator = yarn {
  name = "WindLatestUserProfilingSpark",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["cdr.py"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/cdr.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["user_profiling.sh"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/user_profiling.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["aree_roma.csv"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/aree_roma.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["user_profiling.py"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/user_profiling.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["field2col_simulated.csv"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/field2col_simulated.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestUserProfilingSpark/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./user_profiling.sh"
    }
  }
}