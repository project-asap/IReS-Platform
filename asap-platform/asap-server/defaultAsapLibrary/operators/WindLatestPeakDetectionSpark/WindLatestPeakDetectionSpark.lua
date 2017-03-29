operator = yarn {
  name = "WindLatestPeakDetectionSpark",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["presence.sh"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/presence.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["cdr.py"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/cdr.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["aree_roma.csv"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/aree_roma.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["field2col_simulated.csv"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/field2col_simulated.csv",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["presence.py"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/presence.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestPeakDetectionSpark/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./presence.sh"
    }
  }
}