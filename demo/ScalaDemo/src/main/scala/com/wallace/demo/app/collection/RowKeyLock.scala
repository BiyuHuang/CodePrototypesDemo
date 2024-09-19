package com.wallace.demo.app.collection

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.{ReentrantLock, ReentrantReadWriteLock}
import java.util.concurrent.{ConcurrentHashMap, locks}

/**
 * Author: biyu.huang
 * Date: 2023/11/3 11:35
 * Description:
 */
class RowKeyLock[T] {
  private val locks: ConcurrentHashMap[T, KeyLock] = new ConcurrentHashMap[T, KeyLock]()

  def lock(key: T): Unit = {
    val keyLock: KeyLock = this.locks.compute(key,
      (_, v) => if (v == null) new KeyLock() else v.countLock())
    keyLock.lock.lock()
  }

  def unlock(key: T): Unit = {
    val keyLock: KeyLock = this.locks.get(key)
    keyLock.lock.unlock()
    if (keyLock.deductLock() == 0) {
      locks.remove(key, keyLock)
    }
  }

  private final class KeyLock {
    val lock: ReentrantLock = new ReentrantLock()

    private val lockCount: AtomicInteger = new AtomicInteger(1)

    def countLock(): KeyLock = {
      lockCount.incrementAndGet()
      this
    }

    def deductLock(): Int = lockCount.decrementAndGet()
  }
}

class CustomMap[K, V] {
  private val map: ConcurrentHashMap[K, V] = new ConcurrentHashMap[K, V]()
  private val lock: ReentrantReadWriteLock = new locks.ReentrantReadWriteLock()

  def put(key: K, value: V): Unit = {
    lock.writeLock().lock()
    try {
      map.put(key, value)
    } finally {
      lock.writeLock().unlock()
    }
  }

  def get(key: K): Option[V] = {
    lock.readLock().lock()
    try {
      Option(map.get(key))
    } finally {
      lock.readLock().unlock()
    }
  }

  def remove(key: K): Unit = {
    lock.writeLock().lock()
    try {
      map.remove(key)
    } finally {
      lock.writeLock().unlock()
    }
  }

  def foreach(f: (K, V) => Unit): Unit = {
    lock.readLock().lock()
    try {
      val iterator = map.entrySet().iterator()
      while (iterator.hasNext) {
        val entry = iterator.next()
        f(entry.getKey, entry.getValue)
      }
    } finally {
      lock.readLock().unlock()
    }
  }
}
