/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.common.config

import java.util.Properties

import scala.collection.Seq

/**
  * Created by wallace on 2018/8/29.
  */
object DataLoaderConfig {
  val staticConfig: Properties = {
    val props = new Properties()

    props
  }

  val dynamicConfig: Properties = {
    val props = new Properties()

    props
  }

  object ConfigType {
    val MasterAnt = "masterants"
    val WorkerAnt = "workerants"
    val DataRule = "datarules"
    val all = Seq(MasterAnt, WorkerAnt, DataRule)
  }

  object ConfigKeys {

  }

  object ConfigDefaults {

  }

}
