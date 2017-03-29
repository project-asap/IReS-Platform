operator = yarn {
  name = "WindLatestODMatrixSpark",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["cdr.py"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/cdr.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["od.py"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/od.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["aree_roma.csv"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/aree_roma.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["od.sh"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/od.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["field2col_simulated.csv"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/field2col_simulated.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestODMatrixSpark/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./od.sh"
    }
  }
}