/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
  * Created by wallace on 2017/10/28.
  */
class SystemEnvUtilsUnitSpec extends UnitSpec {
  teamID should "do unit test for getUserDir" in {
    val res = SystemEnvUtils.getUserDir
    val expect = "CodePrototypesDemo"
    log.info(s"UserDir: $res.")
    res.contains(expect) shouldBe true
  }

  teamID should "do unit test for getFileSeparator" in {
    val os = SystemEnvUtils.getPropsByKey("os.name")
    val res = SystemEnvUtils.getFileSeparator
    val expect: String = os.toLowerCase match {
      case v if v.contains("windows") => "\\"
      case _ => "/"
    }
    res shouldBe expect
  }
}
