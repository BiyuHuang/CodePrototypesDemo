package com.hackerforfuture.codeprototypes.dataloader.clusters

/**
 * Author: biyu.huang
 * Date: 2023/11/2 11:30
 * Description:
 */
trait EventHandler {

  def handleHeartbeatEvent(): Unit

  def handleRegisterEvent(): Unit

  def handleStopEvent(): Unit

  def handleRegisterTimeout(): Unit

  def handelCheckHeartbeatEvent(): Unit
}
