operator = yarn {
  name = "WindLatestPeakPublisher",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["description"] = {
       file = "asapLibrary/operators/WindLatestPeakPublisher/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["presence_publisher.py"] = {
       file = "asapLibrary/operators/WindLatestPeakPublisher/presence_publisher.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["presence_publisher.sh"] = {
       file = "asapLibrary/operators/WindLatestPeakPublisher/presence_publisher.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./presence_publisher.sh"
    }
  }
}