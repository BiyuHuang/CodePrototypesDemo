/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.uniqueid

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong}

/**
  * Created by wallace on 2019/4/4.
  */
class UniqueID {

  // SnowFlake - 64bit
  private val workerIdBits: Long = 10L
  private val sequenceBits: Long = 12L
  private val workerIdShift: Long = sequenceBits
  private val timeStampLeftShit: Long = workerIdBits + sequenceBits
  private val lastTimeStamp: AtomicLong = new AtomicLong(-1L)
  private val workerId: AtomicInteger = new AtomicInteger(0)
  private val sequence: AtomicInteger = new AtomicInteger(0)
  private val sequenceMask = -1L ^ (-1L << sequenceBits) // sequence Mask = 4095
  private val maxWorkerId = -1L ^ (-1L << workerIdBits) // Max worker id = 1023

  private val twepoch = 1288834974657L
  private val initFalg: AtomicBoolean = new AtomicBoolean(false)

  def genUniqueID(): Option[Long] = {
    val timestamp: Long = System.currentTimeMillis()
    if (initFalg.compareAndSet(false, true)) {
      val wid: Int = workerId.get() << workerIdShift
      if (lastTimeStamp.compareAndSet(timestamp, timestamp)) {

        val ts: Long = (timestamp - twepoch) << timeStampLeftShit
        val seq: Long = sequence.getAndIncrement() & sequenceMask

        Some(ts | wid | seq)
      } else {

      }
    }
    None
  }
}
