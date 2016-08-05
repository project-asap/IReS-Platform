operator = yarn {
  name = "Pagerank Spark",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["pagerank_spark.sh"] = {
       file = "asapLibrary/operators/PageRank_Spark/pagerank_spark.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./pagerank_spark.sh"
    }
  }
}
