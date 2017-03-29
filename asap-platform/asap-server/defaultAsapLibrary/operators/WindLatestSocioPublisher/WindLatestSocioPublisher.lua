operator = yarn {
  name = "WindLatestSocioPublisher",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestSocioPublisher/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["socio_publisher.py"] = {
       file = "asapLibrary/operators/WindLatestSocioPublisher/socio_publisher.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["socio_publisher.sh"] = {
       file = "asapLibrary/operators/WindLatestSocioPublisher/socio_publisher.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./socio_publisher.sh"
    }
  }
}