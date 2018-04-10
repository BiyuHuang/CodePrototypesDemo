package com.wallace.demo.app.utils

import java.io.File

import com.wallace.demo.app.UnitSpec

/**
  * Created by 10192057 on 2018/4/3 0003.
  */
class FileUtilsUnitSpec extends UnitSpec {
  val fileName = "./demo/ScalaDemo/src/test/resources/test.xml"
  teamID should "do unit test for readXMLFile" in {
    val res: Map[String, FileUtils.AlgMetadata] = FileUtils.readXMLConfigFile(fileName)
    //    res.foreach {
    //      x =>
    //        log.info(s"${x._1}, ${x._2}")
    //    }
    log.info(s"${res("t5_100005").toString}")


  }
}
