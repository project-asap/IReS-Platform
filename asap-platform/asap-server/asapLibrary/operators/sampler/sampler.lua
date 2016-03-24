operator = yarn {
  name = "line sampler",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["sampler.sh"] = {
       file = "asapLibrary/operators/sampler/sampler.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./sampler.sh"
    }
  }
}
