/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.snmp

import com.wallace.UnitSpec

/**
  * Created by wallacehuang on 2017/7/26.
  */
class SnmpPDUAnalysisUnitSpec extends UnitSpec {
  teamID should "do unit test for SnmpPDUAnalysis" in {
    val startTime = System.currentTimeMillis()
    log.info(s"Start time: $startTime")
    val res: List[String] = new SnmpPDUAnalysis(s"${System.getProperty("user.dir")}/src/test/resources/currentAlarmTable.csv").process()
    val endTime = System.currentTimeMillis()
    log.info(s"End time: $endTime, and cost time : ${(endTime - startTime) * 1.0 / 1000} s.")

    //    res.foreach(x => log.info(x))
    res.size shouldBe 27
    res.contains("COMM:EMS=502463710549|11|执行数据库命令失败。|6") shouldBe true
    res.contains("sdrmgr:OMMOID=irpx8xyj-2@sbn=110@NodeMe=0,Equipment=1,rack=1,shelf=1,board=1|3|干接点号: 0|20420") shouldBe true
  }

}
