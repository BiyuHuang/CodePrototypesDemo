/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.utils

/**
  * Created by wallace on 2018/7/9.
  */
object AntThreadFactory {

  def createThread(threadName: String, runnable: Runnable): Thread = {
    createThread(threadName, runnable, daemon = false)
  }

  def createThread(threadName: String, runnable: Runnable, daemon: Boolean): Thread = {
    val thread: Thread = new Thread(runnable, threadName)
    thread.setDaemon(daemon)
    thread
  }

}
