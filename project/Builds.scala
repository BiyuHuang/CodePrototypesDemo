import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object Builds extends Build {

  //----------------------------------------
  // modules in common
  //----------------------------------------
  lazy val sparkDemo = project.in(file("demo/SparkDemo")).settings(name := NamePrefix + "SparkDemo").
    settings(Common.settings: _*).
    settings(libraryDependencies ++= Dependencies.sparkAppDependencies).
    settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
    settings(assemblyJarName in assembly := s"${NamePrefix}SparkDemo_0.0.1.jar")
//    .settings(mainClass in assembly := Some("com.demo.wallace.Test"))

  // lazy val testConsumer = project.in(file("rdd/test")).settings(name := NamePrefix + "test").
  //   settings(Common.settings: _*).
  //   settings(libraryDependencies ++= Dependencies.sparkAppDependencies).
  //   settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
  //   settings(assemblyJarName in assembly := s"${NamePrefix}test_0.0.1.jar")

  // lazy val intelligentAnalysis = project.in(file("rdd/IntelligentAnalysis")).settings(name := NamePrefix + "IntelligentAnalysis").
  //   settings(Common.settings: _*).
  //   settings(libraryDependencies ++= Dependencies.sparkAppDependencies).
  //   settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
  //   settings(assemblyJarName in assembly := s"${NamePrefix}intelligentanalysis_0.0.1.jar")

  val NamePrefix = "HackerForFuture_"
}
