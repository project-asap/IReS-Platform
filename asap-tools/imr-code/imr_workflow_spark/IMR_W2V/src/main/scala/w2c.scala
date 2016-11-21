import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.mllib.linalg
import org.apache.spark.util.{Vector => SV}
import org.apache.log4j._

object W2C extends App{
  /* Logger initialization */
  val layout = new SimpleLayout
  val appender = new FileAppender(layout, "/home/vic/imr.log", false)
  val logger = LogManager.getLogger(W2C.getClass)

  logger.addAppender(appender)
  logger.setLevel(Level.ALL)
  logger.info("Starting")

  var inputPath = null.asInstanceOf[String]
  var outputPath = null.asInstanceOf[String]
  var corpus = null.asInstanceOf[String]
  val op = args(0)

  val sparkConf = new SparkConf().setAppName("IMR Word2Vec Workflow")
  val sc = new SparkContext(sparkConf)
  var model = null.asInstanceOf[Word2VecModel]

  op match {
    case "sm" => {
      this.inputPath = args(1)
      this.corpus = args(1)
      this.outputPath = args(2)
      trainAndSaveModel
      logger.info(s"Model saved at: ${outputPath}!")
    }
    case "sv" => {
      this.inputPath = args(1)
      this.corpus = args(2)
      this.outputPath = args(3)
      println("OK")
      saveVectors
      logger.info(s"Vectors saved at: ${outputPath}")
    }
    case _ => {
      println("Invalid argument")
      System.exit(0)
    }
  }

  def trainAndSaveModel: Unit ={
    logger.info("Training Word2Vec model")
    logger.info(s"Input: ${this.input}")
    logger.info(s"Output: ${this.outputPath}")

    val w2c = new Word2Vec().setVectorSize(200)
    this.model = w2c.fit(this.input.map(_._5))
    this.model.save(sc, outputPath)
  }

  def saveVectors: Unit = {
    logger.info("Vectorization using existing model")
    logger.info(s"Loading model at ${this.inputPath}")
    logger.info("Loading model..")
    model = Word2VecModel.load(sc, this.inputPath)
    logger.info("model loaded.")
    vectorized.saveAsTextFile(this.outputPath)
    logger.info("saved.")
  }

  //Loading input documents
  lazy val input = {
    logger.info(s"preprocessing, ${corpus}")
    sc.textFile(corpus)
      .map(x => x.split(";"))
      .map(x => (x(0), x(1), x(2), x(3), x(4)))
      .map(x => (x._1, x._2, x._3, x._4, x._5.split(" ").toSeq))
  }


  /**l
    * Vectorize each document - Transforming it into a [[linalg.Vector]]
    * */
  lazy val vectorized = input.map{ x =>
      (x._1, x._2, x._3, x._4, vectorizeDocument(x._5))
  }

  /**
    * Vectorize each word - Transforming it into a [[linalg.Vector]] using [[Word2VecModel.transform()]]
    * */
  lazy val vectorizeWord = (word: String) => {
    try{
      model.transform(word)
    } catch{
      case _: Exception => linalg.Vectors.zeros(200)
    }
  }

  lazy val vectorizeDocument = (doc: Seq[String]) => {
    val bv = doc.map{x => vectorizeWord(x)}
      .map{doc => SV(doc.toArray)}
      .reduce(_+_) / doc.size

    linalg.Vectors.dense(bv.elements)
  }


}