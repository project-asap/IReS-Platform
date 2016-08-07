operator = yarn {
  name = "Pagerank Java",
  timeout = 10000,
  memory = 4096,
  cores = 1,
  container = {
    instances = 1,
    --env = base_env,
    resources = {
    ["pagerank_java.sh"] = {
       file = "asapLibrary/operators/PageRank_Java/pagerank_java.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	},
    ["testPageRank.jar"] = {
	    file = "asapLibrary/operators/PageRank_Java/testPageRank.jar",
	    type = "file",               -- other value: 'archive'
	    visibility = "application"  -- other values: 'private', 'public'
      }
    },
    command = {
	base = "./pagerank_java.sh"
    }
  }
}
