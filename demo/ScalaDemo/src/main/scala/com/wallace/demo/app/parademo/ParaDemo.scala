/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.parademo

import scala.collection.parallel.mutable.ParArray
import scala.concurrent.forkjoin.ForkJoinPool

/**
  * Created by wallace on 2019/6/20.
  */
object ParaDemo {

  def main(args: Array[String]): Unit = {
    //TODO processing one by one
    Array(1, 2, 3, 4, 5, 6, 7).map {
      x =>
        println(s"[Thread: ${Thread.currentThread().getName}] Start to process $x @${System.currentTimeMillis()}")
        Thread.sleep(1000L)
        x + 1
    }

    //TODO concurrent processing
    import scala.collection.parallel._
    val pc: ParArray[Int] = mutable.ParArray(1, 2, 3, 5, 6, 7)
    val forkJoinPool: ForkJoinPool = new ForkJoinPool(7)
    pc.tasksupport = new ForkJoinTaskSupport(forkJoinPool)
    pc.map {
      x =>
        println(s"[Thread: ${Thread.currentThread().getName}] Start to process $x @${System.currentTimeMillis()}")
        Thread.sleep(1000L)
        x + 1
    }
    println(s"[Thread: ${Thread.currentThread().getName}] Succeed to process @${System.currentTimeMillis()}")


  }
}
