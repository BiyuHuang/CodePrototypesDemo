import sbt._

object Dependencies {
  val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "2.1.0" % "test",
    "mysql" % "mysql-connector-java" % "5.1.31" % "test",
    "org.apache.kafka" % "kafka-clients" % "0.8.2.1" ,
    "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.4.1"
  )

  val sparkDependencies: Seq[ModuleID] = Seq()

  val sparkAppDependencies: Seq[ModuleID] = testDependencies ++ sparkDependencies
}
