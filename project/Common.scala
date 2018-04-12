import sbt.Keys._
import sbt.{Def, _}
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}

object Common {
  val appVersion = "0.0.1"

  lazy val copyDependencies: TaskKey[Unit] = TaskKey[Unit]("copy-dependencies")
  version := target.toString

  def copyDepTask: Def.Setting[Task[Unit]] = copyDependencies <<= (update, crossTarget, scalaVersion, target) map {
    (updateReport, out, scalaVer, tar) => {
      updateReport.allFiles foreach {
        srcPath =>
          val destPath = out / "lib" / srcPath.getName
          IO.copyFile(srcPath, destPath, preserveLastModified = true)
      }
    }
  }

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := "2.11.8",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    unmanagedBase := baseDirectory.value / "../../lib",
    resolvers += Opts.resolver.mavenLocalFile,
    copyDepTask,
    assemblyJarName in assembly := s"${name.value}_${version.value}.jar",
    test in assembly := {},
    assemblyMergeStrategy in assembly := {
      case PathList(ps@_*) if ps.last endsWith ".xml" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".properties" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".thrift" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".class" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".xsd" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".dtd" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith ".css" => MergeStrategy.first
      //      case PathList(ps@_*) if ps.last endsWith ".txt" => MergeStrategy.first

      //      case PathList(ps@_*) if ps.last endsWith ".jar" => MergeStrategy.first
      //      case PathList(ps@_*) if ps.last endsWith ".providers" => MergeStrategy.first
      //      case PathList(ps@_*) if ps.last endsWith "mailcap" => MergeStrategy.first
      case x =>
        val oldStrategy = (mergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
}
