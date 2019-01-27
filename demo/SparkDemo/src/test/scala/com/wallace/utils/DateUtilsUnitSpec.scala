/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.utils

import java.text.SimpleDateFormat

import com.wallace.UnitSpec

/**
  * Created by wallace on 2019/1/27.
  */
class DateUtilsUnitSpec extends UnitSpec {
  teamID should "do unit test for addDate" in {
    val res: String = DateUtils.addDate("2018-12-31", -10)
    val expect: String = "2018-12-21"
    res shouldBe expect
  }

  teamID should "do unit test for addDate with different dateFormat" in {
    val res: String = DateUtils.addDate("2018-12-31 10:20:30.0", -10, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss"))
    val expect: String = "2018-12-21"
    res shouldBe expect
  }
}
