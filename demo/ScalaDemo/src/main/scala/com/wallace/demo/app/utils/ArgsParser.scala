package com.wallace.demo.app.utils

import com.typesafe.scalalogging.LazyLogging
import com.wallace.demo.app.common.Using
import org.apache.commons.cli.{CommandLine, GnuParser, Options}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{FileNotFoundException, FileReader}
import java.util.Properties

/**
 * Author: wallace huang
 * Date: 2022/11/14 16:39
 * Description: Args Parser
 */
object ArgsParser extends Using with LazyLogging {
  def getProperty(props: Properties, key: String): String = {
    if (props.getProperty(key) == "") {
      null
    } else {
      props.getProperty(key)
    }
  }

  def getProperty(props: Properties, key: String, splitter: String): Array[String] = {
    val re: String = props.getProperty(key)
    if (re == null || re == "") {
      null
    } else {
      re.split(splitter)
    }
  }

  def checkHDFSPath(path: String, conf: Configuration): Boolean = {
    using(FileSystem.get(conf)) {
      fs =>
        if (fs.exists(new Path(path))) {
          true
        } else {
          logger.info(s"$path not existed")
          false
        }
    }
  }

  def loadProps(args: Array[String],
    defaultProps: String = "default.properties"): Properties = {
    val cmd: CommandLine = parseArguments(args)
    val props: Properties = loadJobProps(cmd.getOptionValue("f"), defaultProps)
    val conf: Array[String] = cmd.getOptionValues("conf")
    if (null != conf && conf.nonEmpty) {
      conf.map {
        confItem =>
          val (k, v) = parseCommandConf(confItem)
          props.put(k, v)
      }
    }
    props
  }

  private def parseArguments(args: Array[String]): CommandLine = {
    val parser: GnuParser = new GnuParser
    val options: Options = new Options()
    options.addOption("f", true, "properties file of job")
    options.addOption("conf", true, "param setting of the job")
    parser.parse(options, args)
  }

  @throws[FileNotFoundException]
  private def loadJobProps(path: String, defaultProps: String): Properties = {
    val jobProps: Properties = new Properties()
    if (null != path && path.nonEmpty) {
      jobProps.load(new FileReader(path))
    } else {
      if (defaultProps != null && defaultProps.nonEmpty) {
        jobProps.load(this.getClass.getClassLoader.getResourceAsStream(defaultProps))
      }
    }
    jobProps
  }

  private def parseCommandConf(confStr: String): (String, String) = {
    val (k, v) = confStr.split("=", 2).toSeq match {
      case Seq(k, v) => (k, v)
      case _ =>
        throw new IllegalArgumentException(s"can't parse CMD conf $confStr")
    }
    (k, v)
  }
}
