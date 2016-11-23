operator = yarn {
  name = "HelloWorld",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "/opt/asap-server/asapLibrary/operators/HelloWorld/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["HelloWorld.lua"] = {
       file = "/opt/asap-server/asapLibrary/operators/HelloWorld/HelloWorld.lua",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["HelloWorld.sh"] = {
       file = "/opt/asap-server/asapLibrary/operators/HelloWorld/HelloWorld.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./HelloWorld.sh"
    }
  }
}
