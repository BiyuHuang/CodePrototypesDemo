import sbt._

object Dependencies {
  val commonDependencies: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % "1.2.1",
    "org.scalatest" % "scalatest_2.11" % "2.2.4",
    "mysql" % "mysql-connector-java" % "5.1.31" % "test",
    "ch.ethz.ganymed" % "ganymed-ssh2" % "build210",
    "org.slf4j" % "slf4j-api" % "1.7.7",
    "org.slf4j" % "slf4j-log4j12" % "1.7.16"
  )

  val sparkDependencies: Seq[ModuleID] = Seq(
    "org.apache.kafka" % "kafka-clients" % "0.8.2.1",
    "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.0.0",
    "org.apache.spark" % "spark-core_2.11" % "2.0.0",
    "org.apache.spark" % "spark-sql_2.11" % "2.0.0",
    "org.apache.spark" % "spark-hive_2.11" % "2.0.0",
    "org.apache.spark" % "spark-streaming_2.11" % "2.0.0",
    "org.apache.spark" % "spark-mllib_2.11" % "2.0.0"
  )

  val sparkAppDependencies: Seq[ModuleID] = commonDependencies ++ sparkDependencies
}