/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import com.hackerforfuture.codeprototypes.dataloader.schedule.AntScheduler
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable

/**
  * Created by wallace on 2018/6/23.
  */
object MasterAnt {

  //TODO 1. Start a FixedSizePool, and run a mater's actor.

  //TODO 2. Register worker, State Manage etc.

  val threadPool = new AntScheduler(1)

  @volatile var antConf: Config = ConfigFactory.empty()


  val workers: mutable.HashSet[WorkerDetailInfo] = new mutable.HashSet[WorkerDetailInfo]()


  val stateManager: StateManager = new StateManager(antConf)


}
