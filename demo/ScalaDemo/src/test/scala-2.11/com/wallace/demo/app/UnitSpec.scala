package com.wallace.demo.app

import com.wallace.demo.app.utils.FuncRuntimeDur
import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by Wallace on 2016/11/6.
  */
trait UnitSpec extends FlatSpec with ShouldMatchers with FuncRuntimeDur {
  protected val teamID = "Wallace Huang"

  var runTimes: Int = 1

  def runBenchmarkTest(utMsg: String)(testFunc: => Any): Unit = {
    //TODO Run Benchmark Test
    require(runTimes >= 10000, s"Benchmark need to execute at least 10000 times And runTimes = $runTimes.")
    teamID should s"do $utMsg" in {
      val costTime = runtimeDuration(testFunc, runTimes)
      log.info(s"RunTimes: $runTimes, CostTime: $costTime ms.")
    }
  }
}
