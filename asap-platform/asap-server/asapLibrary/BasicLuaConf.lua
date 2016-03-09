MASTER_JAR_LOCATION = "/home/forth/testing_asap4all/IReS-Platform/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"

CP = "/home/forth/hadoop-2.7.1/etc/hadoop:/home/forth/hadoop-2.7.1/share/hadoop/common/lib/*:/home/forth/hadoop-2.7.1/share/hadoop/common/*:/home/forth/hadoop-2.7.1/share/hadoop/hdfs:/home/forth/hadoop-2.7.1/share/hadoop/hdfs/lib/*:/home/forth/hadoop-2.7.1/share/hadoop/hdfs/*:/home/forth/hadoop-2.7.1/share/hadoop/yarn/lib/*:/home/forth/hadoop-2.7.1/share/hadoop/yarn/*:/home/forth/hadoop-2.7.1/share/hadoop/mapreduce/lib/*:/home/forth/hadoop-2.7.1/share/hadoop/mapreduce/*:/home/forth/hadoop-2.7.1/lib:/home/forth/hadoop-2.7.1/contrib/capacity-scheduler/*.jar"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", CP, "./master.jar"}, ":"),
}

-- The actual distributed shell job.
operator = yarn {
  name = "Asap master",
  timeout = 1000000000,
  memory = 1024,
  cores = 1,
  master = {
    name = "Asap master",
    env = base_env,
    resources = base_resources,
    command = {
      base = "${JAVA_HOME}/bin/java -Xms64m -Xmx128m com.cloudera.kitten.appmaster.ApplicationMaster",
      args = { "-conf job.xml" },
    }
  }
}
