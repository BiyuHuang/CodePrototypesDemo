package com.wallace.demo.app.parsercombinators

import com.wallace.demo.app.UnitSpec
import com.wallace.demo.app.common.AlgMetaData
import com.wallace.demo.app.parsercombinators.parsers.ParserChain
import com.wallace.demo.app.utils.FileUtils

/**
  * Created by 10192057 on 2018/4/13 0013.
  */
class ParsersConstructorUnitSpec extends UnitSpec {
  val fileName = "./demo/ScalaDemo/src/test/resources/test.xml"
  val parsersConfig: Map[String, AlgMetaData] = FileUtils.readXMLConfigFile(fileName)
  teamID should "do unit test for ParsersConstructor.generateParsers" in {
    val parsers: Map[String, ParserChain] = ParsersConstructor.generateParsers(parsersConfig)
    parsers.size shouldBe parsersConfig.size
    val parser = parsers("t6_1000006")
    val res = parser.parse("a1,b2,c3,d4,e5,f6#f7#f8")
    var cnt: Int = 0
    val startTime = System.currentTimeMillis()
    val runTimes: Int = 1000000
    while (cnt < runTimes) {
      parser.parse("a1,b2,c3,d4,e5,f6")
      cnt += 1
    }
    val costTime: Long = System.currentTimeMillis() - startTime
    log.info(s"RunTimes: $runTimes, CostTime: $costTime ms, Rate: ${runTimes * 1000.0 / costTime}.")

    res shouldBe "f6,b2,c3,d4,e5,f7,f8"
  }
}
