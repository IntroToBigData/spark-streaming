package learn.spark

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object Test {
    def main(args: Array[String]) {
      val logFile = "test.txt"
      val conf = new SparkConf().setAppName("TestApplication").setMaster("local[2]")
      val sc = new SparkContext(conf)
      val logData = sc.textFile(logFile, 2).cache()
      val numAs = logData.filter(line => line.contains("a")).count()
      val numBs = logData.filter(line => line.contains("b")).count()
      println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
    }
}