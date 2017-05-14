package com.wallace.demo.app.utils

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by wallacehuang on 2017/5/13.
  */
class ConcatStringUtilsUnitSpec extends FlatSpec with ShouldMatchers {
  "Wallace Huang" should "do unit test for the function: concatStrUtils" in {
    val input = "a1,b2,c3,d4,e5,4,n1,n11,n12,n13,n2,n21,n22,n23,n3,n31,n32,n33,n4,n41,n42,n43,f6,g8,h9"
    val res = ConcatStringUtils.concatStrUtils(input, 5, 4)
    val expect = Array("a1", "b2", "c3", "d4", "e5", "4", "n1$n2$n3$n4", "n11$n21$n31$n41", "n12$n22$n32$n42", "n13$n23$n33$n43", "f6", "g8", "h9")

    res shouldBe expect
  }

  "Wallace Huang" should "do one more unit test for the function: concatStrUtils" in {
    val input = "a1,b2,c3,d4,4,n1,n11,n12,n2,n21,n22,n3,n31,n32,n4,n41,n42,f6,g8,h9"
    val res = ConcatStringUtils.concatStrUtils(input, 4, 3)
    val expect = Array("a1", "b2", "c3", "d4", "4", "n1$n2$n3$n4", "n11$n21$n31$n41","n12$n22$n32$n42", "f6", "g8", "h9")

    res shouldBe expect
  }

}
