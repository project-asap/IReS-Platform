operator = yarn {
  name = "Pagerank Hama",
  timeout = 10000,
  memory = 1024,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["pagerank_hama.sh"] = {
       file = "asapLibrary/operators/PageRank_Hama/pagerank_hama.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	}
    },
    command = {
	base = "./pagerank_hama.sh"
    }
  }
}
