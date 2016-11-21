name := "SparkOps"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"org.apache.spark" %% "spark-core" % "1.4.0",
	"org.apache.spark" %% "spark-mllib" % "1.4.0",
	"org.json4s" %% "json4s-jackson" % "3.2.11"
	)
