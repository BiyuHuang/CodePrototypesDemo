package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
  * Created by 10192057 on 2018/5/23 0023.
  */
class MathUtilsUnitSpec extends UnitSpec {
  teamID should "do unit test for intToByteArray" in {
    val value: Int = 2018
    val res: Array[Byte] = MathUtils.intToByteArray(value)
    val expect: Array[Byte] = Array[Byte](0, 0, 7, -30)
    res.length shouldBe 4
    res shouldBe expect
  }

  teamID should "do unit test for byteArrayToInt" in {
    val byteArray: Array[Byte] = Array[Byte](0, 0, 7, -30)
    val res: Int = MathUtils.byteArrayToInt(byteArray)
    res shouldBe 2018
  }


  teamID should "do unit test for longToByteArray" in {
    val value: Long = 2147483648L
    val res: Array[Byte] = MathUtils.longToByteArray(value)
    val expect: Array[Byte] = Array[Byte](0, 0, 0, 0, -128, 0, 0, 0)
    res.length shouldBe 8
    res shouldBe expect
  }

  teamID should "do unit test for byteArrayToLong" in {
    val byteArray: Array[Byte] = Array[Byte](0, 0, 0, 0, -128, 0, 0, 0)
    val res: Long = MathUtils.byteArrayToLong(byteArray)
    res shouldBe 2147483648L
  }
}
