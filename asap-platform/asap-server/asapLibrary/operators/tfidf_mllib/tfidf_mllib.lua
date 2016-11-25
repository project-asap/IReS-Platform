operator = yarn {
  name = "tfidf_mllib",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["tfidf_mllib.sh"] = {
       file = "asapLibrary/operators/tfidf_mllib/tfidf_mllib.sh",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["spark_tfidf.py"] = {
       file = "asapLibrary/operators/tfidf_mllib/spark_tfidf.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        },
	  ["description"] = {
       file = "asapLibrary/operators/tfidf_mllib/description",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
        base = "./tfidf_mllib.sh"
    }
  }
}