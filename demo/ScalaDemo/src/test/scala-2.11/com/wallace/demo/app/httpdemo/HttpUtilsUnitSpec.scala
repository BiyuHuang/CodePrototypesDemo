package com.wallace.demo.app.httpdemo

import com.wallace.demo.app.UnitSpec

/**
  * Created by 10192057 on 2018/4/26 0026.
  */
class HttpUtilsUnitSpec extends UnitSpec {
  val queryConfigUrl: String = "http://10.9.234.32:8988/uniportal/zdh/entryServlet?taskName=queryConfigValueByName"
  val cipherUrl: String = "http://10.9.234.32:8988/uniportal/zdh/entryServlet?taskName=cipher"
  teamID should "do unit test for sendPost: user" in {
    val params: String = s"""{"serviceName":"vmaxplat","serviceInstanceId":"vmaxplat","roleName":"","configItemName":"sparkSubmit.defaultCluster.vmaxplat.ssh.user"}"""
    val result: String = HttpUtils.sendPost(queryConfigUrl, params)
    log.info(result)
    val tempRes: Map[String, String] = result.replaceAll("[\"{}]", "").split(",").map {
      elem =>
        val temp = elem.split(":", -1)
        (temp.head.trim, temp.last.trim)
    }.toMap
    val expectValue: String = tempRes.getOrElse("value", "")
    result shouldBe "{\"value\":\"mr\",\"fileName\":\"/home/netnumen/ems/ums-server/utils/vmax-conf/serviceaddress.properties\",\"key\":\"sparkSubmit.defaultCluster.vmaxplat.ssh.user\",\"itemType\":\"StringParam\"}"
    expectValue shouldBe "mr"
  }

  teamID should "do unit test for sendPost: password" in {
    val params: String = s"""{"serviceName":"vmaxplat","serviceInstanceId":"vmaxplat","roleName":"","configItemName":"sparkSubmit.defaultCluster.vmaxplat.ssh.password"}"""
    val result: String = HttpUtils.sendPost(queryConfigUrl, params)
    log.info(result)
    val tempRes: Map[String, String] = result.replaceAll("[\"{}]", "").split(",").map {
      elem =>
        val temp = elem.split(":", -1)
        (temp.head.trim, temp.last.trim)
    }.toMap
    val expectValue: String = tempRes.getOrElse("value", "")
    expectValue shouldBe "rSlBSrAveiOToO1eROw9ngJB3293iv+EGKfFZjGYy1E="
  }

  teamID should "do unit test for sendPost: decrypt" in {
    val params: String = s"""{"operateType":"decrypt","password":"rSlBSrAveiOToO1eROw9ngJB3293iv+EGKfFZjGYy1E="}"""
    val result: String = HttpUtils.sendPost(cipherUrl, params)
    log.info(result)
    result shouldBe "DAP_user_mr_2017"
  }

  teamID should "do unit test for sendPost: encrypt" in {
    val params: String = s"""{"operateType":"encrypt","password":"DAP_user_mr_2017"}"""
    val result: String = HttpUtils.sendPost(cipherUrl, params)
    log.info(result)
    result shouldBe "rSlBSrAveiOToO1eROw9ngJB3293iv+EGKfFZjGYy1E="
  }
}
