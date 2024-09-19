package com.wallace.demo.app.algorithmdemo

import com.wallace.demo.app.UnitSpec

/**
 * @Project: CodePrototypesDemo
 * @Package: com.wallace.demo.app.algorithmdemo
 * @Author: 80286616
 * @Company: notalk tech
 * @CreateDate: 2022/2/22
 * @Description:
 */
class AlgDemoUnitSpec extends UnitSpec {
  val mockData: Array[Array[Int]] = Array(Array(1, 4, 7, 11, 15), Array(2, 5, 8, 12, 19), Array(3, 6, 9, 16, 22),
    Array(10, 13, 14, 17, 24), Array(18, 21, 23, 26, 30))
  teamID should "invoke findNumberIn2DArray correctly" in {
    val notExistedTargets: Array[Int] = Array(0, 20, 25, 31)
    val expect: Boolean = false
    notExistedTargets.foreach {
      target =>
        val actual: Boolean = AlgDemo.findNumberIn2DArray(mockData, target)
        actual shouldBe expect
    }
    val existedTarget: Array[Int] = Array(1, 9, 15, 18, 30)
    existedTarget.foreach {
      target =>
        val actual: Boolean = AlgDemo.findNumberIn2DArray(mockData, target)
        actual shouldBe !expect
    }
  }
}
