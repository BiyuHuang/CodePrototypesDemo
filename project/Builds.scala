import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object Builds extends Build {

  //----------------------------------------
  // modules in common
  //----------------------------------------
  lazy val sparkDemo: Project = project.in(file("demo/SparkDemo")).settings(name := NamePrefix + "SparkDemo").
    settings(Common.settings: _*).
    settings(libraryDependencies ++= Dependencies.sparkAppDependencies).
    settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
    settings(assemblyJarName in assembly := s"${NamePrefix}SparkDemo_0.0.1.jar")
    .settings(mainClass in assembly := Some("com.wallace.spark.SparkDemo.DataFrameDemo.DataFrameDemo"))

  lazy val scalaDemo: Project = project.in(file("demo/ScalaDemo")).settings(name := NamePrefix + "ScalaDemo").
    settings(Common.settings: _*).
    settings(libraryDependencies ++= Dependencies.commonDependencies).
    settings(libraryDependencies ++= Dependencies.hdfsDependencies).
    settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
    settings(assemblyJarName in assembly := s"${NamePrefix}ScalaDemo_0.0.1.jar")

  lazy val dataLoader: Project = project.in(file("demo/DataLoader")).settings(name := NamePrefix + "DataLoader").
    settings(Common.settings: _*).
    settings(libraryDependencies ++= Dependencies.dataLoaderDependencies).
    settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
    settings(assemblyJarName in assembly := s"${NamePrefix}DataLoader_0.0.1.jar").
    settings(mainClass in assembly := Some("com.hackerforfuture.codeprototypes.dataloader.DataLoader"))

  // lazy val intelligentAnalysis = project.in(file("rdd/IntelligentAnalysis")).settings(name := NamePrefix + "IntelligentAnalysis").
  //   settings(Common.settings: _*).
  //   settings(libraryDependencies ++= Dependencies.sparkAppDependencies).
  //   settings(assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)).
  //   settings(assemblyJarName in assembly := s"${NamePrefix}intelligentanalysis_0.0.1.jar")

  val NamePrefix = "HackerForFuture_"
}
