CP = "/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/share/hadoop/common/lib/*:/opt/hadoop-2.7.0/share/hadoop/common/*:/opt/hadoop-2.7.0/share/hadoop/hdfs:/opt/hadoop-2.7.0/share/hadoop/hdfs/lib/*:/opt/hadoop-2.7.0/share/hadoop/hdfs/*:/opt/hadoop-2.7.0/share/hadoop/yarn/lib/*:/opt/hadoop-2.7.0/share/hadoop/yarn/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/lib/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/*:/contrib/capacity-scheduler/*.jar:/opt/hadoop-2.7.0/share/hadoop/yarn/*:/opt/hadoop-2.7.0/share/hadoop/yarn/lib/*"

base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP}, ":"),
}

operator = yarn {
  name = "tfidf scikit",
  timeout = 100000000,
  memory = 1024,
  cores = 1,
  env = base_env,
  container = {
    instances = 1,
    resources = {
    ["tfidf_scikit.sh"] = {
       file = "asapLibrary/operators/TF_IDF_scikit/tfidf_scikit.sh",
      		type = "file",               -- other value: 'archive'
      		visibility = "application"  -- other values: 'private', 'public'
	},
    ["tfidf_scikit.py"] = {
       file = "asapLibrary/operators/TF_IDF_scikit/tfidf_scikit.py",
                type = "file",               -- other value: 'archive'
                visibility = "application"  -- other values: 'private', 'public'
        }
    },
    command = {
	base = "./tfidf_scikit.sh"
    }
  }
}
