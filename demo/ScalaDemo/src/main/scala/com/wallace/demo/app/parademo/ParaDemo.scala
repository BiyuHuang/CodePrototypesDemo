/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.parademo

import java.util.concurrent.ForkJoinPool
import scala.collection.parallel.mutable.ParArray

/**
  * Created by wallace on 2019/6/20.
  */
object ParaDemo {
  val testData: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  def main(args: Array[String]): Unit = {
    //TODO processing one by one
    println("#### processing one by one")
    testData.map {
      x =>
        println(s"[Thread: ${Thread.currentThread().getName}] Start to process $x @${System.currentTimeMillis()}")
        Thread.sleep(1000L)
        x + 1
    }
    println(s"[Thread: ${Thread.currentThread().getName}] Succeed to process @${System.currentTimeMillis()}")
    //TODO processing par
    println(s"#### processing par @${scala.collection.parallel.ForkJoinTasks.defaultForkJoinPool.getParallelism}")
    testData.par.map {
      x =>
        println(s"[Thread: ${Thread.currentThread().getName}] Start to process $x @${System.currentTimeMillis()}")
        Thread.sleep(1000L)
        x + 1
    }
    println(s"[Thread: ${Thread.currentThread().getName}] Succeed to process @${System.currentTimeMillis()}")
    //TODO concurrent processing
    println(s"#### concurrent processing @3")
    import scala.collection.parallel._
    val pc: ParArray[Int] = mutable.ParArray(testData: _*)
    val forkJoinPool: ForkJoinPool = new ForkJoinPool(3)
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
