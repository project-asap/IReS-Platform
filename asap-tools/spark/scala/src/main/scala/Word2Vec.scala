import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import java.io._

object Word2Vec {
  def main(args: Array[String]){
  	val conf = new SparkConf().setAppName("Word2Vec CSLab")
  	val sc = new SparkContext(conf)
  	val input = sc.wholeTextFiles(args(0)).map(doc => doc._2.split(" ").toSeq)
  	val bc = sc.broadcast(input)
  	val word2vec = new Word2Vec()	
  
  
  	var start = java.lang.System.currentTimeMillis()
  	val model = word2vec.fit(input)
  	val execTime = java.lang.System.currentTimeMillis - start
  	val operator = "Word2Vec_Spark"
	
	  var output = ("operator" -> operator) ~ 
                 ("exec_time" -> execTime) ~
					       ("input_size" -> input.count())

		val writer = new PrintWriter(new File(s"/var/www/html/${operator}T${start}.json"))
		writer.write(compact(output))
		writer.close()
  }
}
