package com.wallace.common

import org.scalatest.{FlatSpec, _}

/**
  * Created by Wallace on 2016/11/6.
  */
protected[com] trait UnitSpec extends FlatSpec with ShouldMatchers with LogSupport {
  protected val teamID = "Team HackerForFuture"
}
