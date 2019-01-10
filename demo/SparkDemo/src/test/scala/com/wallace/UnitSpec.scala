/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace

import com.wallace.common.LogSupport
import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by Wallace on 2016/11/6.
  */
trait UnitSpec extends FlatSpec with ShouldMatchers with LogSupport {
  protected val teamID = "Wallace Huang"

  def runTest(utMsg: String)(testFunc: => Any): Unit = {
    teamID should s"do $utMsg" in {
      testFunc
    }
  }
}
