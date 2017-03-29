operator = yarn {
  name = "WindLatestODMatrixPublisher",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestODMatrixPublisher/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["od_publisher.py"] = {
       file = "asapLibrary/operators/WindLatestODMatrixPublisher/od_publisher.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["od_publisher.sh"] = {
       file = "asapLibrary/operators/WindLatestODMatrixPublisher/od_publisher.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./od_publisher.sh"
    }
  }
}