package com.wallace.demo.app.roundrobinloop

import java.util.concurrent.{Executors, TimeUnit}
import com.wallace.demo.app.common.LogSupport

/**
 * Author: biyu.huang
 * Date: 2023/4/23 10:53
 * Description:
 */
object RoundRobinService extends LogSupport {
  val periodSeconds: Int = 60
  val queryCount: Int = 30
  val resources: List[String] = List("Resource 1", "Resource 2", "Resource 3", "Resource 4")

  def main(args: Array[String]): Unit = {
    val periodMillis: Int = periodSeconds * 1000
    val queryIntervalMillis: Int = periodMillis / queryCount
    var resourceIndex: Int = 0
    var iteration: Int = 0

    val executorService = Executors.newSingleThreadScheduledExecutor()
    executorService.scheduleWithFixedDelay(() => {
      val resource = resources(resourceIndex)
      logger.info(s"[Iteration: $iteration] Executing query on $resource")
      // Execute query on the selected resource
      resourceIndex = (resourceIndex + 1) % resources.size
      iteration += 1

      if (iteration == (queryCount / 2)) {
        logger.info("shut down service")
        executorService.shutdown()
      }
    }, 10 * 1000L, queryIntervalMillis, TimeUnit.MILLISECONDS)

    try {
      logger.info("start round-robin service")
      executorService.awaitTermination(periodSeconds, TimeUnit.SECONDS)
      logger.info("finished round-robin service")
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
  }
}
