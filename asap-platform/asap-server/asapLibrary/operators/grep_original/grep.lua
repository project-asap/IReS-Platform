operator = yarn {
  name = "Grep",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["grep.sh"] = {
       file = "asapLibrary/operators/grep/grep.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./grep.sh"
    }
  }
}
