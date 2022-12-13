/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec
import com.wallace.demo.app.common.AlgMetaData

/**
  * Created by 10192057 on 2018/4/3 0003.
  */
class FileUtilsUnitSpec extends UnitSpec {
  val fileName = "./demo/ScalaDemo/src/test/resources/test.xml"
  teamID should "do unit test for readXMLFile" in {
    val res: Map[String, AlgMetaData] = FileUtils.readXMLConfigFile(fileName)
    res.foreach {
      x =>
        logger.info(s"${x._1}, ${x._2}")
    }
    //    log.info(s"${res("t2_1000002").toString}")
    //    log.info(s"${res("t5_1000005").toString}")
  }
}
