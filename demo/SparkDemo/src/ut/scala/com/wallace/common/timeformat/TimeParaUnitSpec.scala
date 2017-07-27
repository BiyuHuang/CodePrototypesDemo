package com.wallace.common.timeformat

import java.text.SimpleDateFormat
import java.util.Date

import com.wallace.UnitSpec

/**
  * Created by wallacehuang on 2017/7/26.
  */
class TimeParaUnitSpec extends UnitSpec {
  teamID should "do unit spec for TimePara method: dateFormat(seconds: String)" in {
    val res = TimePara.dateFormat("86401")
    res shouldBe "1970-01-02 00:00:01.000"
  }

  teamID should "do unit spec for TimePara method: dateFormat(seconds: Long)" in {
    val res = TimePara.dateFormat("1")
    res shouldBe "1970-01-01 00:00:01.000"
  }


  teamID should "do unit spec for TimePara method: getCostTime" in {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val res = TimePara.getCostTime(new Date(sdf.parse("2017-07-25 10:30:01.103").getTime), new Date(sdf.parse("2017-07-26 11:30:00.101").getTime))
    res shouldBe "89998.0 s"
  }
}
