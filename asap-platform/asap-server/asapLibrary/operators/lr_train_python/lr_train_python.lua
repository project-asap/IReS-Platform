operator = yarn {
  name = "w2v train",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    resources = {
    ["train.sh"] = {
       file = "asapLibrary/operators/lr_train_python/train.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	},
    ["train.py"] = {
       file = "asapLibrary/operators/lr_train_python/train.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
    ["utils.py"] = {
       file = "asapLibrary/operators/lr_train_python/utils.py",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
        },
    ["config.py"] = {
       file = "asapLibrary/operators/lr_train_python/config.py",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
        },
    ["stop-words_french_1_fr.txt"] = {
       file = "asapLibrary/operators/lr_train_python/stop-words_french_1_fr.txt",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
        },
    ["stop-words_french_2_fr.txt"] = {
       file = "asapLibrary/operators/lr_train_python/stop-words_french_2_fr.txt",
      type = "file",               -- other value: 'archive'
      visibility = "application",  -- other values: 'private', 'public'
        }
    },
    command = {
	base = "./train.sh"
    }
  }
}
