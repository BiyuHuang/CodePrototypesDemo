import sbt._

object Dependencies {
  val commonDependencies: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % "1.2.1",
    "org.scalatest" % "scalatest_2.11" % "2.2.4",
    "mysql" % "mysql-connector-java" % "5.1.31" % "test",
    "ch.ethz.ganymed" % "ganymed-ssh2" % "build210",
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "ch.qos.logback" % "logback-core" % "1.1.2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.akka" %% "akka-actor" % "2.5.3",
    "com.typesafe.akka" %% "akka-remote" % "2.5.3",
    "com.github.pathikrit" %% "better-files" % "3.4.0",
    "com.github.pathikrit" %% "better-files-akka" % "3.4.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
    "com.yammer.metrics" % "metrics-core" % "2.2.0"


  )

  val httpDependencies: Seq[ModuleID] = Seq(
    "org.apache.httpcomponents" % "httpclient" % "4.5.3",
    "org.apache.httpcomponents" % "httpcore" % "4.4.9"
  )

  val hdfsDependencies: Seq[ModuleID] = Seq(
    "org.apache.hadoop" % "hadoop-common" % "2.7.2",
    "org.apache.hadoop" % "hadoop-hdfs" % "2.7.2",
    "org.apache.commons" % "commons-compress" % "1.15",
    "org.apache.parquet" % "parquet-hadoop" % "1.8.1"
  )

  val sparkDependencies: Seq[ModuleID] = Seq(
    "org.apache.kafka" % "kafka-clients" % "0.10.0.1",
    "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.0.2",
    "org.apache.spark" % "spark-streaming-flume_2.11" % "2.0.0",
    "org.apache.spark" % "spark-core_2.11" % "2.0.2",
    "org.apache.spark" % "spark-sql_2.11" % "2.0.2",
    "org.apache.spark" % "spark-hive_2.11" % "2.0.2",
    "org.apache.spark" % "spark-streaming_2.11" % "2.0.2",
    "org.apache.spark" % "spark-mllib_2.11" % "2.0.2"
  )

  val sparkAppDependencies: Seq[ModuleID] = commonDependencies ++ hdfsDependencies ++ sparkDependencies
  val scalaDemoDependencies: Seq[ModuleID] = commonDependencies ++ httpDependencies ++ hdfsDependencies
  val dataLoaderDependencies: Seq[ModuleID] = commonDependencies ++ hdfsDependencies
}