package com.wallace.demo.app.functionaldemo

import com.wallace.demo.app.UnitSpec
import com.wallace.demo.app.functionaldemo.FunctionalDemo.{p0, p2, p3}

/**
  * Created by wallacehuang on 2017/7/26.
  */
class FunctionalDemoUnitSpec extends UnitSpec {
  teamID should "do unit test for Functional Demo" in {
    val a: Int = 3
    val b: BigInt = FunctionalDemo.toBigInt(a)
    log.info(s"${Int.MaxValue}, ${Int.MinValue}, ${b.pow(a)}")
    log.info(s"${p0(1, 2, 3)}") // 6
    log.info(s"${p2(100)}") // 130
    log.info(s"${p3(10, 1)}")
    log.info("[Partial Functions] " + FunctionalDemo.divide(10))
    log.info("[Partial Functions] " + FunctionalDemo.divide1(10))
    log.info("[Partial Functions] " + FunctionalDemo.direction(180))
    log.info("[匿名函数] " + FunctionalDemo.m1(2))
    log.info("[偏应用函数] " + FunctionalDemo.sum(1, 2, 3))
    log.info("Curry 函数] " + FunctionalDemo.curriedSum(5)(6))

    val res_1 = FunctionalDemo.p0(1, 2, 3)
    val res_2 = FunctionalDemo.p2(100)
    val res_3 = FunctionalDemo.p3(10, 1)
    val res_4 = FunctionalDemo.divide(10)
    val res_5 = FunctionalDemo.divide1(10)
    val res_6 = FunctionalDemo.direction(180)
    val res_7 = FunctionalDemo.m1(2)
    val res_8 = FunctionalDemo.sum(1, 2, 3)
    val res_9 = FunctionalDemo.curriedSum(5)(6)

    res_1 shouldBe 6
    res_2 shouldBe 130
    res_3 shouldBe 111
    res_4 shouldBe 10
    res_5 shouldBe 10
    res_6 shouldBe "West"
    res_7 shouldBe 4
    res_8 shouldBe 6
    res_9 shouldBe 11
  }
}
