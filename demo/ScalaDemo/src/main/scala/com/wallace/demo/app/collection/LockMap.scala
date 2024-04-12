package com.wallace.demo.app.collection

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.{Lock, ReentrantLock}

/**
 * Author: biyu.huang
 * Date: 2024/4/12 17:18
 * Description:
 */
class LockMap[K, V] {
  private val hashMap = new ConcurrentHashMap[K, V]
  private val keyLocks = new ConcurrentHashMap[K, Lock]

  def put(key: K, value: V): Unit = {
    val lock = getOrCreateLock(key)
    lock.lock()
    try {
      hashMap.put(key, value)
    } finally {
      lock.unlock()
    }
  }

  def getAndLock(key: K): V = {
    val lock = getOrCreateLock(key)
    lock.lock()
    hashMap.get(key)
  }

  def unlock(key: K): Unit = {
    val lock = getOrCreateLock(key)
    lock.unlock()
  }

  def remove(key: K): Unit = {
    val lock = getOrCreateLock(key)
    lock.lock()
    try {
      hashMap.remove(key)
      keyLocks.remove(key)
    } finally {
      lock.unlock()
    }
  }

  private def getOrCreateLock(key: K): Lock = {
    keyLocks.computeIfAbsent(key, _ => new ReentrantLock())
  }
}