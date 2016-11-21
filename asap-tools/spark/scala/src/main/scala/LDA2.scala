import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext._
import scala.collection.mutable
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.{Vector, Vectors}

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

import java.io._

/*
 * Args
 * 1) Input Path
 * 2) K - Number of topics
 * */

object LDA2 {
	def main(args: Array[String]){
		val conf = new SparkConf().setAppName("LDA_CSLab")
		val sc = new SparkContext(conf)
		val corpus: RDD[String] = sc.wholeTextFiles(args(0)).map(doc => doc._2)
  
		val tokenized: RDD[Seq[String]] = 
			corpus.map(_.toLowerCase.split("\\s"))
			.map(_.filter(_.length > 3)
			.filter(_.forall(java.lang.Character.isLetter)))

		val termCounts: Array[(String, Long)] =
			tokenized.flatMap(_.map(_ -> 1L)).reduceByKey(_ + _).collect().sortBy(-_._2)


		val numStopWords = 20
		val vocabArray: Array[String] = 
			termCounts.takeRight(termCounts.size - numStopWords).map(_._1)
		val vocab: Map[String, Int] = vocabArray.zipWithIndex.toMap

		val documents: RDD[(Long, Vector)] =
  			tokenized.zipWithIndex.map { case (tokens, id) =>
    	val counts = new mutable.HashMap[Int, Double]()
    		tokens.foreach { term =>
	      		if (vocab.contains(term)) {
	        		val idx = vocab(term)
	        		counts(idx) = counts.getOrElse(idx, 0.0) + 1.0
	      		}
    		}
    		(id, Vectors.sparse(vocab.size, counts.toSeq))
  		}
		

		//LDA
		val numberOfTopics = args(1).toInt

		var start = java.lang.System.currentTimeMillis()
		val ldaModel = new LDA()
						.setK(numberOfTopics)
						.setMaxIterations(10)
						.run(documents)
		

		val execTime = java.lang.System.currentTimeMillis - start
		val operator = "LDA_Spark"
    val numDocs = corpus.count()

		var output = ("operator" -> operator) ~ 
					 ("exec_time" -> execTime) ~
           ("k" -> numberOfTopics)~
					 ("input_size" -> numDocs)

		val writer = new PrintWriter(new File(s"/var/www/html/${operator}T${start}.json"))
		writer.write(compact(output))
		writer.close()
	}
}