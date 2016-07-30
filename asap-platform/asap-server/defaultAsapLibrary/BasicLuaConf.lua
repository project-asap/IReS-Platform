IRES_HOME="/root/vpapa/asap/IReS-Platform/"
MASTER_JAR_LOCATION = IRES_HOME .. "/cloudera-kitten/java/master/target/kitten-master-0.2.0-jar-with-dependencies.jar"

YARN_CLASSPATH="/opt/hadoop-2.7.0/etc/hadoop:/opt/hadoop-2.7.0/share/hadoop/common/lib/*:/opt/hadoop-2.7.0/share/hadoop/common/*:/opt/hadoop-2.7.0/share/hadoop/hdfs:/opt/hadoop-2.7.0/share/hadoop/hdfs/lib/*:/opt/hadoop-2.7.0/share/hadoop/hdfs/*:/opt/hadoop-2.7.0/share/hadoop/yarn/lib/*:/opt/hadoop-2.7.0/share/hadoop/yarn/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/lib/*:/opt/hadoop-2.7.0/share/hadoop/mapreduce/*:/opt/hadoop-2.7.0/contrib/capacity-scheduler/*.jar:/opt/hbase-0.94.5n/lib:/opt/hbase-0.94.5n/conf"

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
