package com.wallace.spark.common

import scala.collection.JavaConversions._

/**
  * Created by wallace on 16-6-16.
  */
object DemoConfig extends App with ProjConfig {
  setConfigFiles("msgproducer.conf")

  /**
    * Kafka Settings
    */

  val brokers = config.getStringList("brokers.list").toList.map(_.split(",")).map(x=> (x(0),x(1).toInt)).toMap
  val topics = config.getString("topics")
  val messagePerSec = config.getString("messagePerSec")

  log.error(
    s"""
       |$brokers
       |$topics
       |$messagePerSec
     """.stripMargin)
}
