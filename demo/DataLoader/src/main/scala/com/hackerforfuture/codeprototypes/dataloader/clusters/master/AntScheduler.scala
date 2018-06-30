/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import java.util.concurrent.TimeUnit

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport

/**
  * Created by wallace on 2018/6/24.
  */

/**
  * A scheduler for running jobs
  *
  * This interface controls a job scheduler that allows scheduling either repeating background jobs
  * that execute periodically or delayed one-time actions that are scheduled in the future.
  */
trait Scheduler {

  /**
    * Initialize this scheduler so it is ready to accept scheduling of tasks
    */
  def startup(): Unit

  /**
    * Shutdown this scheduler. When this method is complete no more executions of background tasks will occur.
    * This includes tasks scheduled with a delayed execution.
    */
  def shutdown(): Unit

  /**
    * Check if the scheduler has been started
    */
  def isStarted: Boolean

  /**
    * Schedule a task
    *
    * @param name   The name of this task
    * @param delay  The amount of time to wait before the first execution
    * @param period The period with which to execute the task. If < 0 the task will execute only once.
    * @param unit   The unit for the preceding times.
    */
  def schedule(name: String, fun: () => Unit, delay: Long = 0, period: Long = -1, unit: TimeUnit = TimeUnit.MILLISECONDS): Unit
}

class AntScheduler(threadNum: Int,
                   val threadNamePrefix: String = "kafka-scheduler-",
                   daemon: Boolean = true) extends Scheduler with LogSupport {
  /**
    * Initialize this scheduler so it is ready to accept scheduling of tasks
    */
  override def startup(): Unit = {

  }

  /**
    * Shutdown this scheduler. When this method is complete no more executions of background tasks will occur.
    * This includes tasks scheduled with a delayed execution.
    */
  override def shutdown(): Unit = {

  }

  /**
    * Check if the scheduler has been started
    */
  override def isStarted: Boolean = ???

  /**
    * Schedule a task
    *
    * @param name   The name of this task
    * @param delay  The amount of time to wait before the first execution
    * @param period The period with which to execute the task. If < 0 the task will execute only once.
    * @param unit   The unit for the preceding times.
    */
  override def schedule(name: String, fun: () => Unit, delay: Long, period: Long, unit: TimeUnit): Unit = ???
}

