<<<<<<< HEAD
IRES_HOME="/root/vpapa/asap/IReS-Platform/"
=======
ES_HOME="/home/bill/PhD/projects/asap/asap4all/IReS-Platform"
>>>>>>> temp
MASTER_JAR_LOCATION = IRES_HOME .. "/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"

YARN_CLASSPATH="/home/bill/PhD/projects/yarn/etc/hadoop:/home/bill/PhD/projects/yarn/share/hadoop/common/lib/*:/home/bill/PhD/projects/yarn/share/hadoop/common/*:/home/bill/PhD/projects/yarn/share/hadoop/hdfs:/home/bill/PhD/projects/yarn/share/hadoop/hdfs/lib/*:/home/bill/PhD/projects/yarn/share/hadoop/hdfs/*:/home/bill/PhD/projects/yarn/share/hadoop/yarn/lib/*:/home/bill/PhD/projects/yarn/share/hadoop/yarn/*:/home/bill/PhD/projects/yarn/share/hadoop/mapreduce/lib/*:/home/bill/PhD/projects/yarn/share/hadoop/mapreduce/*:/contrib/capacity-scheduler/*.jar"

-- Resource and environment setup.
base_resources = {
  ["master.jar"] = { file = MASTER_JAR_LOCATION }
}
base_env = {
  CLASSPATH = table.concat({"${CLASSPATH}", YARN_CLASSPATH, "./master.jar"}, ":"),
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
