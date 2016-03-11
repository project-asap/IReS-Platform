operator = yarn {
  name = "w2v train",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    resources = {
    ["w2v_train_python.sh"] = {
       file = "asapLibrary/operators/w2v_train_python/w2v_train_python.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./w2v_train_python.sh"
    }
  }
}
