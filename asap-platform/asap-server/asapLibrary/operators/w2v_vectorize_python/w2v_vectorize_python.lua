operator = yarn {
  name = "w2v_vectorize_python",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    resources = {
    ["w2v_vectorize_python.sh"] = {
       file = "asapLibrary/operators/w2v_vectorize_python/w2v_vectorize_python.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	},
    ["stop-words_french_1_fr.txt"] = {
       file = "asapLibrary/operators/w2v_vectorize_python/stop-words_french_1_fr.txt",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	},
    ["stop-words_french_2_fr.txt"] = {
       file = "asapLibrary/operators/w2v_vectorize_python/stop-words_french_2_fr.txt",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./w2v_vectorize_python.sh"
    }
  }
}
