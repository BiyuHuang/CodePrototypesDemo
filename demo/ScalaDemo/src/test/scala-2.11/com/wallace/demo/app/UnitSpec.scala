package com.wallace.demo.app

import com.wallace.demo.app.common.LogSupport
import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by Wallace on 2016/11/6.
  */
trait UnitSpec extends FlatSpec with ShouldMatchers with LogSupport {
  protected val teamID = "Team HackerForFuture"
}
