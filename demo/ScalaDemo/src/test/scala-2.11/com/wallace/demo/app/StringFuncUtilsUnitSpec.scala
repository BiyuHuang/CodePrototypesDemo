package com.wallace.demo.app

import com.wallace.demo.app.common.LogSupport
import com.wallace.demo.app.utils.StringFuncUtils
import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by Wallace on 2017/1/14.
  */
class StringFuncUtilsUnitSpec extends FlatSpec with ShouldMatchers with LogSupport {

  "HackerForFuture" should "test StringFuncUtils: empty elements" in {
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

  "HackerForFuture" should "test StringFuncUtils: empty string" in {
    val str = ""
    val expect = ""
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 1
    result.head shouldBe expect
  }

  "HackerForFuture" should "test StringFuncUtils: one element" in {
    val str = "elem1"
    val expect = "elem1"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 1
    result.head shouldBe expect
  }

  "HackerForFuture" should "test StringFuncUtils: two elements" in {
    val str = "elem1,elem2"
    val expect = "elem2"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 2
    result.last shouldBe expect
  }

  "HackerForFuture" should "test StringFuncUtils: three elements" in {
    val str = "elem1,elem2,\"elem3=1,elem4=2,elem5=3\""
    val expect = "elem3=1,elem4=2,elem5=3"
    val result = StringFuncUtils.splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@" + elem + "@")
    }
    result.length shouldBe 3
    result.last shouldBe expect
  }

  "HackerForFuture" should "test StringFuncUtils: more than three elements" in {
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
}
