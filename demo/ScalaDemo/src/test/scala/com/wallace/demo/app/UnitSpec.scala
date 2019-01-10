/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

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
