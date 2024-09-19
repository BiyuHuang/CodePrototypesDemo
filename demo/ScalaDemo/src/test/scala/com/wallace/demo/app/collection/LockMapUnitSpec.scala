package com.wallace.demo.app.collection

import com.wallace.demo.app.UnitSpec
import org.apache.hadoop.yarn.webapp.hamlet2.HamletSpec.THEAD

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

/**
 * Author: biyu.huang
 * Date: 2024/4/12 17:19
 * Description:
 */
class LockMapUnitSpec extends UnitSpec {
  teamID should "operate LockMap" in {
    val lockMap = new LockMap[String, String]

    // Put some values
    lockMap.put("key1", "value1")
    lockMap.put("key2", "value2")
    lockMap.put("key3", "value3")

    // Concurrently access and print values
    logger.info("getAndLock key1")
    lockMap.getAndLock("key1")
    Thread.sleep(1000)
    lockMap.unlock("key1")

    val future1 = Future {
      logger.info("feature1: key1 -> " + lockMap.getAndLock("key1"))
      Thread.sleep(1000)
      lockMap.unlock("key1")
    }

    val future2 = Future {
      logger.info("feature2: key1 -> " + lockMap.getAndLock("key1"))
      Thread.sleep(3000)
      lockMap.unlock("key1")
    }

    val future3 = Future {
      logger.info("feature3: key1 -> " + lockMap.getAndLock("key1"))
      Thread.sleep(5000)
      lockMap.unlock("key1")
    }

    // Wait for all futures to complete
    val allFutures = Future.sequence(Seq(future1, future2, future3))
    val result = scala.concurrent.Await.result(allFutures, 10.seconds)

    // Try to retrieve removed key
    lockMap.remove("key2")
    logger.info(lockMap.getAndLock("key2")) // Should print null
  }
}
