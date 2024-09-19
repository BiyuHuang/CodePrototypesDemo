package com.wallace.utils

import org.apache.commons.cli
import org.apache.commons.cli.{GnuParser, Options}

import java.io.{FileNotFoundException, FileReader}
import java.util.Properties

object ArgsParser {
  def loadConfig(args: Array[String]): Properties = {
    val cmd = parseArguments(args)
    val props = loadJobProps(cmd.getOptionValue("f"))
    val conf = cmd.getOptionValues("conf")
    if (null != conf && conf.nonEmpty) {
      conf.map {
        confItem =>
          val (k, v) = parseCommandConf(confItem)
          props.put(k, v)
      }
    }
    props
  }

  def parseCommandConf(confItem: String): (String, String) = {
    val (key, value) = confItem.split("=", 2).map(_.trim).toList match {
      case k :: v :: Nil => (k, v)
      case _ =>
        throw new IllegalArgumentException(s"can't parse the config: ${confItem}")
    }
    (key, value)
  }

  @throws[FileNotFoundException]
  private def loadJobProps(path: String): Properties = {
    val jobProps = new Properties()
    if (null != path && path.nonEmpty) {
      jobProps.load(new FileReader(path))
    }
    jobProps
  }

  def parseArguments(args: Array[String]): cli.CommandLine = {
    val parser = new GnuParser
    val options = new Options()
    options.addOption("f", true, "properties file of job")
    options.addOption("conf", true, "param setting of the job")
    parser.parse(options, args)
  }
}
