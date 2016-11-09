import sbt._

object Dependencies {
  val testDependencies: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % "1.2.1",
    "org.scalatest" % "scalatest_2.11" % "2.2.4",
    "mysql" % "mysql-connector-java" % "5.1.31" % "test",
    "ch.ethz.ganymed" % "ganymed-ssh2" % "build210",
    "org.apache.kafka" % "kafka-clients" % "0.8.2.1",
    "org.apache.spark" % "spark-streaming-kafka_2.11" % "1.6.2"
  )

  val sparkDependencies: Seq[ModuleID] = Seq(
    "org.apache.spark" % "spark-core_2.11" % "2.0.0",
    "org.apache.spark" % "spark-sql_2.11" % "2.0.0",
    "org.apache.spark" % "spark-hive_2.11" % "2.0.0",
    "org.apache.spark" % "spark-streaming_2.11" % "2.0.0",
    "org.apache.spark" % "spark-mllib_2.11" % "2.0.0"
  )

  val sparkAppDependencies: Seq[ModuleID] = testDependencies ++ sparkDependencies
}