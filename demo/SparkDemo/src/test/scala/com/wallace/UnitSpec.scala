package com.wallace

import com.wallace.common.LogSupport
import org.scalatest.{FlatSpec, ShouldMatchers}

trait UnitSpec extends FlatSpec with ShouldMatchers with LogSupport {
  final val teamID = "Hacker"

  def runTest(testMsg: String)(op: => Unit): Unit = {
    teamID should s"$testMsg" in {
      op
    }
  }
}
