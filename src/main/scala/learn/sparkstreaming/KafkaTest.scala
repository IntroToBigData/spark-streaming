package learn.sparkstreaming

import java.util.Properties

import _root_.kafka.serializer.StringDecoder
import kafka.producer._

import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import ua_parser.{Client, Parser}

object KafkaWordCount {
  def main(args: Array[String]) {
    val Array(zkQuorum, group, topics, numThreads) = Array("localhost:2181", "consumer-test", "test", "1")
    val sparkConf = new SparkConf().setAppName("KafkaWordCount").setMaster("local[2]")
    val ssc =  new StreamingContext(sparkConf, Seconds(3))
    ssc.checkpoint("checkpoint")

    val topicMap = topics.split(",").map((_,numThreads.toInt)).toMap
    val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1L)).reduceByKeyAndWindow(_ + _, _ - _, Minutes(1), Seconds(6), 2)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }

}

// Produces some random words between 1 and 100.
object KafkaWordCountProducer {

  def main(args: Array[String]) {

    val Array(brokers, topic, messagesPerSec, wordsPerMessage) = Array("localhost:9092", "test", "1", "4")

    // Zookeeper connection properties
    val props = new Properties()
    props.put("metadata.broker.list", brokers)
    props.put("serializer.class", "kafka.serializer.StringEncoder")

    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)

    // Send some messages
    while (true) {
      val messages = (1 to messagesPerSec.toInt).map { messageNum =>
        val str = (1 to wordsPerMessage.toInt).map(x => scala.util.Random.nextInt(10).toString)
          .mkString(" ")

        new KeyedMessage[String, String](topic, str)
      }.toArray

      producer.send(messages: _*)
      Thread.sleep(100)
    }
  }
}

object KafkaUAP {

  def main (args: Array[String]) {
    //val agentString: String = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; fr; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5,gzip(gfe),gzip(gfe)"
    //print(uaParser(agentString))

    val sparkConf = new SparkConf().setAppName("UserAgentParser").setMaster("local[2]")
    val ssc =  new StreamingContext(sparkConf, Seconds(3))
    ssc.checkpoint("checkpoint")

    writeToKafka(readFromKafka("uap-input", ssc).map(x=>uaParser(x)))

    ssc.start()
    ssc.awaitTermination()

  }

  def readFromKafka(topics: String, ssc: StreamingContext): DStream[String] = {
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> "localhost:9092")
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topicsSet)
    messages.map(_._2)
  }

  def writeToKafka(parsedRDD: DStream[String]): Unit = {
    parsedRDD.foreachRDD(rdd => {
      rdd.foreachPartition(partition => {

        val props: Properties = new Properties()
        props.put("metadata.broker.list", "localhost:9092")
        props.put("producer.type", "sync")
        props.put("serializer.class", "kafka.serializer.StringEncoder")
        //props.put("batch.num.messages", "3")
        val producer = new Producer[String, String](new ProducerConfig(props))

        partition.foreach(message => {
          val keyedMessage = new KeyedMessage[String, String]("uap-output", message)
          producer.send(keyedMessage)
        })
      })
    })
  }

  def uaParser(input: String): String = {
    var output: String = ""
    val uaParser: Parser = new Parser
    val client: Client = uaParser.parse(input)

    output += client.userAgent.family
    output += ", "
    output += client.os.family
    output += ", "
    output += client.device.family

    output
  }

}