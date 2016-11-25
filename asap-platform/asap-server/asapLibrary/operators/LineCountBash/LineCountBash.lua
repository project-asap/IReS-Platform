operator = yarn {
  name = "LineCountBash",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["count_lines.sh"] = {
       file = "asapLibrary/operators/LineCountBash/count_lines.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["description"] = {
       file = "asapLibrary/operators/LineCountBash/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./count_lines.sh"
    }
  }
}