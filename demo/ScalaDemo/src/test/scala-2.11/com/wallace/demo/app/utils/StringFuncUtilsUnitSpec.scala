package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
  * Created by Wallace on 2017/1/14.
  */
class StringFuncUtilsUnitSpec extends UnitSpec {

  "Wallace Huang" should "test StringFuncUtils: empty elements" in {
    val str = ",,,"
    val expect = ""
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 3
    result(0) shouldBe expect
    result(2) shouldBe expect
  }

  "Wallace Huang" should "test StringFuncUtils: empty string" in {
    val str = ""
    val expect = ""
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 1
    result.head shouldBe expect
  }

  "Wallace Huang" should "test StringFuncUtils: one element" in {
    val str = "elem1"
    val expect = "elem1"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 1
    result.head shouldBe expect
  }

  "Wallace Huang" should "test StringFuncUtils: two elements" in {
    val str = "elem1,elem2"
    val expect = "elem2"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 2
    result.last shouldBe expect
  }

  "Wallace Huang" should "test StringFuncUtils: three elements" in {
    val str = "elem1,elem2,\"elem3=1,elem4=2,elem5=3\""
    val expect = "elem3=1,elem4=2,elem5=3"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 3
    result.last shouldBe expect
  }

  "Wallace Huang" should "test StringFuncUtils: more than three elements" in {
    val str = "elem1,elem2,\"elem3=1,elem4=2,elem5=3\",elem6,\"elem7=4,elem8=5,elem9=6\",elem10"
    val expect1 = "elem3=1,elem4=2,elem5=3"
    val expect2 = "elem7=4,elem8=5,elem9=6"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 6
    result(2) shouldBe expect1
    result(4) shouldBe expect2
  }

  "Wallace Huang" should "do unit test for the function: concatStrUtils" in {
    val input = "a1,b2,c3,d4,e5,4,n1,n11,n12,n13,n2,n21,n22,n23,n3,n31,n32,n33,n4,n41,n42,n43,f6,g8,h9"
    val res = ConcatStringUtils.concatCols(input, 5, 4)
    val expect = Array("a1", "b2", "c3", "d4", "e5", "4", "n1$n2$n3$n4", "n11$n21$n31$n41", "n12$n22$n32$n42", "n13$n23$n33$n43", "f6", "g8", "h9")

    res shouldBe expect
  }

  "Wallace Huang" should "do one more unit test for the function: concatStrUtils" in {
    val input = "a1,b2,c3,d4,4,n1,n11,n12,n2,n21,n22,n3,n31,n32,n4,n41,n42,f6,g8,h9"
    val res = ConcatStringUtils.concatCols(input, 4, 3)
    val expect = Array("a1", "b2", "c3", "d4", "4", "n1$n2$n3$n4", "n11$n21$n31$n41", "n12$n22$n32$n42", "f6", "g8", "h9")

    res shouldBe expect
  }
  "Wallace Huang" should "do unit test for: countKeyWord" in {
    val input = "Hello world and Hello again. It's wonderful day!"
    val res: Map[String, Int] = StringFuncUtils.countKeyWord(input, " ")
    res.foreach(x => log.info(s"KeyWord: ${x._1}, Count: ${x._2}"))
    val expect = 2
    res.getOrElse("Hello", "") shouldBe expect
    res.getOrElse("wonderful", "") shouldBe 1
  }

  "Wallace Huang" should "do unit test for: convertStrToFixedFormat" in {
    val res = StringFuncUtils.convertStrToFixedFormat("25525511135")

    res.contains("255.255.11.135") shouldBe true
    res.length shouldBe 2
  }

  "Wallace Huang" should "do unit test for: extractFields" in {
    val res: String = StringFuncUtils.extractFields("2018-4-8 17:19:19,666666,1,109.01,32.34,true,1,2,3,4,5,6")
    val expect: String = "2018-4-8 17:19:19,666666,1,true,1,109.01,32.34"

    runTimes = 1000000
    val costTime: Double = runtimeDuration(StringFuncUtils.extractFields("2018-4-8 17:19:19,666666,1,109.01,32.34,true,1,2,3,4,5,6"), runTimes)
    log.info(s"[BenchmarkTest] Times: $runTimes, CostTime: $costTime ms, Rate(Records/sec): ${runTimes / (costTime / 1000)}")
    res shouldBe expect
  }
}
