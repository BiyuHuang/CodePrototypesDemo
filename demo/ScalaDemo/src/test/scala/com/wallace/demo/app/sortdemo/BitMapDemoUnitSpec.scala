package com.wallace.demo.app.sortdemo

import com.wallace.demo.app.UnitSpec

/**
 * Author: biyu.huang
 * Date: 2024/5/8 15:16
 * Description:
 */
class BitMapDemoUnitSpec extends UnitSpec {
  teamID should "do unit test for BitMap" in {
    val bitMap = new BitMapDemo(65, 32).bitMap
    bitMap.setBit(0)
    bitMap.setBit(1)
    bitMap.setBit(2)
    bitMap.setBit(3)
    bitMap.setBit(4)
    bitMap.setBit(7)
    bitMap.setBit(32)
    bitMap.setBit(65)

    assertResult(true)(bitMap.exists(1))
    assertResult(true)(bitMap.exists(32))
    assertResult(true)(bitMap.exists(65))
    assertResult(false)(bitMap.exists(8))
    assertResult(8)(bitMap.bitCount())

    logger.info("bitCount => {}", bitMap.bitCount())
    logger.info(bitMap.toString)
  }
}
