package com.wallace

import com.wallace.utils.ArgsParser

import java.util.Properties
import scala.jdk.CollectionConverters.propertiesAsScalaMapConverter

/**
 * Author: wallace
 * Date: 2022/5/17 00:15
 * Description:
 */
object Boot {
  def main(args: Array[String]): Unit = {
    val props: Properties = ArgsParser.loadConfig(args)
    props.asScala.foreach(println)
  }

  def lookUpJobs(jobName: String): Class[_] = {
    // TODO
    ???
  }
}
