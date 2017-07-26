package com.wallace.snmp

import com.wallace.UnitSpec

/**
  * Created by wallacehuang on 2017/7/26.
  */
class SnmpPDUAnalysisUnitSpec extends UnitSpec {
  teamID should "do unit test for SnmpPDUAnalysis" in {
    val startTime = System.currentTimeMillis()
    log.info(s"Start time: $startTime")
    val res: List[String] = new SnmpPDUAnalysis("demo/SparkDemo/data/currentAlarmTable.csv").process()
    val endTime = System.currentTimeMillis()
    log.info(s"End time: $endTime, and cost time : ${(endTime - startTime) * 1.0 / 1000} s.")

    res.foreach(x => log.info(x))
    res.size shouldBe 27
    res.contains("COMM:EMS=502463710549|11|执行数据库命令失败。|6") shouldBe true
    res.contains("sdrmgr:OMMOID=irpx8xyj-2@sbn=110@NodeMe=0,Equipment=1,rack=1,shelf=1,board=1|3|干接点号: 0|20420") shouldBe true
  }

}
