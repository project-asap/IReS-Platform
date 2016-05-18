operator = yarn {
  name = "Execute Hello world",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["HelloWorld.sh"] = {
       file = "asapLibrary/operators/HelloWorld/HelloWorld.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./HelloWorld.sh"
    }
  }
}
