package com.wallace.common

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * com.wallace.common
  * Created by 10192057 on 2017/12/19 0019.
  */
trait UnitSpecTrait extends FlatSpec with ShouldMatchers with LogSupport {
  val teamId = "Wallace Huang"

  def runTest(utMsg: String)(testFunc: => Any): Unit = {
    teamId should s"do $utMsg" in {
      testFunc
    }
  }
}
