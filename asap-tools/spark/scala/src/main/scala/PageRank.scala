import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext._

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import java.io._

object PageRank{
	def main(args: Array[String]){
		val conf = new SparkConf().setAppName("PageRank_CSLab")
		val sc = new SparkContext(conf)
		val inputGraph = GraphLoader.edgeListFile(sc, args(0))
		val start = java.lang.System.currentTimeMillis
    val alpha = args(1).toDouble
		val ranks = inputGraph.pageRank(alpha).vertices
		val execTime = java.lang.System.currentTimeMillis - start
		val operator = "PageRank_Spark"

		var output = ("operator" -> operator) ~ 
                 ("exec_time" -> execTime) ~
                 ("alpha" -> alpha)
	}
}