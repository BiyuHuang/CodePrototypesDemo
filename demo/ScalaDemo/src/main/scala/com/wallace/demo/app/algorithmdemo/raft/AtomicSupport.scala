package com.wallace.demo.app.algorithmdemo.raft

import java.util.concurrent.Semaphore

/**
 * Author: biyu.huang
 * Date: 2023/1/18 14:56
 * Description:
 */
trait AtomicSupport {
  protected def atomicBlock[R](semaphore: Semaphore)(block: => R): R = {
    try {
      semaphore.acquire(1)
      block
    } finally {
      semaphore.release(1)
    }
  }
}
