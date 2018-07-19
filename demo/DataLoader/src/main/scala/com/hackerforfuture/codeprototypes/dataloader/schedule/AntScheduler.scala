/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.schedule

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ScheduledThreadPoolExecutor, ThreadFactory, TimeUnit}

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.utils.AntThreadFactory

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
                   val threadNamePrefix: String = "Ant-scheduler-",
                   daemon: Boolean = true) extends Scheduler with LogSupport {
  private val schedulerThreadId = new AtomicInteger(0)
  private var executor: Option[ScheduledThreadPoolExecutor] = None

  /**
    * Initialize this scheduler so it is ready to accept scheduling of tasks
    */
  override def startup(): Unit = {
    log.debug("Initializing task scheduler.")
    this synchronized {
      if (isStarted) throw new IllegalStateException("This scheduler has already been started!")
      executor = Some(new ScheduledThreadPoolExecutor(threadNum))
      executor.get.setContinueExistingPeriodicTasksAfterShutdownPolicy(false)
      executor.get.setExecuteExistingDelayedTasksAfterShutdownPolicy(false)
      executor.get.setThreadFactory(new ThreadFactory() {
        def newThread(runnable: Runnable): Thread =
          AntThreadFactory.createThread(threadNamePrefix + schedulerThreadId.getAndIncrement(), runnable, daemon)
      })
    }
  }

  /**
    * Shutdown this scheduler. When this method is complete no more executions of background tasks will occur.
    * This includes tasks scheduled with a delayed execution.
    */
  override def shutdown(): Unit = {
    log.debug("Shutting down task scheduler.")
    // We use the local variable to avoid NullPointerException if another thread shuts down scheduler at same time.
    val cachedExecutor: Option[ScheduledThreadPoolExecutor] = this.executor
    if (cachedExecutor.isDefined) {
      this synchronized {
        cachedExecutor.get.shutdown()
        this.executor = None
      }
      cachedExecutor.get.awaitTermination(1, TimeUnit.DAYS)
    }
  }

  def resizeThreadPool(newSize: Int): Unit = {
    executor.get.setCorePoolSize(newSize)
  }

  def scheduleOnce(name: String, fun: () => Unit): Unit = {
    schedule(name, fun, delay = 0L, period = -1L, unit = TimeUnit.MILLISECONDS)
  }

  /**
    * Schedule a task
    *
    * @param name   The name of this task
    * @param delay  The amount of time to wait before the first execution
    * @param period The period with which to execute the task. If < 0 the task will execute only once.
    * @param unit   The unit for the preceding times.
    */
  override def schedule(name: String, fun: () => Unit, delay: Long, period: Long, unit: TimeUnit): Unit = {
    log.debug("Scheduling task %s with initial delay %d ms and period %d ms."
      .format(name, TimeUnit.MILLISECONDS.convert(delay, unit), TimeUnit.MILLISECONDS.convert(period, unit)))
    this synchronized {
      ensureRunning()
      val runnable = new Runnable {
        override def run(): Unit = {
          try {
            log.debug("Beginning execution of scheduled task '%s'.".format(name))
            fun()
          } catch {
            case t: Throwable => log.error("Uncaught exception in scheduled task '" + name + "'", t)
          } finally {
            log.debug("Completed execution of scheduled task '%s'.".format(name))
          }
        }
      }
      if (period >= 0) {
        executor.get.scheduleAtFixedRate(runnable, delay, period, unit)
      } else {
        executor.get.schedule(runnable, delay, unit)
      }
    }
  }

  private def ensureRunning(): Unit = {
    if (!isStarted) throw new IllegalStateException("Kafka scheduler is not running.")
  }

  /**
    * Check if the scheduler has been started
    */
  override def isStarted: Boolean = {
    this synchronized {
      executor.isDefined
    }
  }
}

