package com.wallace.demo.app.utils

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by wallacehuang on 2017/5/13.
  */
class CombineMultiVersionUnitSpec extends FlatSpec with ShouldMatchers {

  val handler = new CombineMultiVersion
  "Wallace Huang" should "do unit test for the function: just one column" in {
    val testSQL = "c030,c031,c032,rsrp,rsrq,c068"
    val res = handler.combineMultiVersion(testSQL, Array("rsrp"))
    val expect = "c030,c031,c032,rsrp_repaired as rsrp,rsrq,c068"
    res shouldBe expect
  }

  "Wallace Huang" should "do unit test for the function: tow columns" in {
    val testSQL = "c030,c031,c032,rsrp,rsrq,c068"
    val res = handler.combineMultiVersion(testSQL, Array("rsrq ", " rsrp "))
    val expect = "c030,c031,c032,rsrp_repaired as rsrp,rsrq_repaired as rsrq,c068"
    res shouldBe expect
  }

  "Wallace Huang" should "do unit test for the function: column's index out of range" in {
    val testSQL = "c030,c031,c032,rsrp,rsrq,c068"
    val res = handler.combineMultiVersion(testSQL, Array("sinr"))
    val expect = "c030,c031,c032,rsrp,rsrq,c068"
    res shouldBe expect
  }

}
