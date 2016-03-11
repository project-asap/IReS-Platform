operator = yarn {
  name = "w2v_vectorize_spark",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  nodes = "slave-2",
  labels = "slave-2",
  container = {
    instances = 1,
    resources = {
    ["w2v_vectorize_spark.sh"] = {
       file = "asapLibrary/operators/w2v_vectorize_spark/w2v_vectorize_spark.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./w2v_vectorize_spark.sh"
    }
  }
}
