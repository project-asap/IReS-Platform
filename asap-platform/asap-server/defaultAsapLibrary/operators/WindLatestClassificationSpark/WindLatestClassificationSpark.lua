operator = yarn {
  name = "WindLatestClassificationSpark",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestClassificationSpark/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["classification.sh"] = {
       file = "asapLibrary/operators/WindLatestClassificationSpark/classification.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["cdr.py"] = {
       file = "asapLibrary/operators/WindLatestClassificationSpark/cdr.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["stereo_type_classification.py"] = {
       file = "asapLibrary/operators/WindLatestClassificationSpark/stereo_type_classification.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["utils.py"] = {
       file = "asapLibrary/operators/WindLatestClassificationSpark/utils.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./classification.sh"
    }
  }
}