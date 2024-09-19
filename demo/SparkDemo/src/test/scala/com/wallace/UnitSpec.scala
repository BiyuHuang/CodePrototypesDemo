package com.wallace

import com.wallace.common.LogSupport
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait UnitSpec extends AnyFlatSpec with Matchers with LogSupport {
  final val teamID = "Hacker"

  def runTest(testMsg: String)(op: => Unit): Unit = {
    teamID should s"$testMsg" in {
      op
    }
  }
}
