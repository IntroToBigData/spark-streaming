name := "spark-demo"

version := "1.0"

scalaVersion := "2.10.4"

val additionalRepos = Seq(
  "Twitter Maven Repo" at "http://maven.twttr.com/"
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.1"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.5.1"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.5.1"

libraryDependencies += "ua_parser" % "ua-parser" % "1.3.0"

