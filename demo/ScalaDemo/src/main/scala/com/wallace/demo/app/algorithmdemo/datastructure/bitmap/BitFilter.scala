package com.wallace.demo.app.algorithmdemo.datastructure.bitmap

import com.wallace.demo.app.common.LogSupport

import java.lang.Math._
import scala.collection.mutable.ArrayBuffer
import scala.util.hashing.MurmurHash3

/**
 * Author: biyu.huang
 * Date: 2023/6/13 14:48
 * Description:
 */
class BitFilter[T <: AnyVal](n: Long, p: Double = 0.0001) {
  private final val RANDOM_NUM: Int =
    ((random * System.currentTimeMillis).longValue >> 32 & 0xffff).toInt
  // The size of bitmap: m
  private final val m: Long = ceil(-1.0 * n * log(p * 0.9999) / pow(log(2), 2)).longValue
  assert(m / 64 <= Int.MaxValue, s"too many elements: $n")
  private final val bitMap: BitMap = new BitMap(m)
  // The number of Hash method: k
  private final val k: Int = ceil(0.7 * m / n).longValue.toInt
  private final val seeds: Array[Long] = generatePrimeArray(k, RANDOM_NUM)
  // False Positive Rate: fpr
  private final val fpr: BigDecimal = BigDecimal.valueOf(pow(1 - pow(E, -n * k * 1.0 / m), k))

  /**
   * @return size of bitmap in bytes
   */
  def getSize: Double = this.m / 8.0

  /**
   * @return number of Hash method
   */
  def getK: Long = this.k

  /**
   * @return the actual False Positive Rate
   */
  def getFPR: BigDecimal = this.fpr

  def hash(key: T, seed: Int): Long = {
    MurmurHash3.bytesHash(s"$key".getBytes, seed) & (m - 1)
  }

  def add(key: T): Unit = {
    seeds.foreach {
      seed =>
        bitMap.insert(hash(key, seed.toInt))
    }
  }

  /**
   * @param key bit filter key
   * @return False when key doesn't exists, True means key might exists
   */
  def exists(key: T): Boolean = {
    seeds.forall {
      seed =>
        bitMap.exists(hash(key, seed.toInt))
    }
  }

  def generatePrimeArray(k: Int, start: Int = RANDOM_NUM): Array[Long] = {
    def isPrime(number: Long): Boolean = {
      if (number <= 3) return number > 1
      var i: Long = 2L
      while (i <= sqrt(number)) {
        if (number % i == 0) return false
        i += 1
      }
      true
    }

    val res: ArrayBuffer[Long] = new ArrayBuffer[Long]()
    var number: Long = start
    while (res.size < k) {
      if (isPrime(number)) res.append(number)
      number += 1
    }
    res.result().toArray
  }

  def show(num: Int): Unit = this.bitMap.show(num)

  def bitCount: Long = this.bitMap.bitCount
}

object BitFilter extends LogSupport {
  def main(args: Array[String]): Unit = {
    val bitFilter: BitFilter[Int] = new BitFilter[Int](100)
    Array(1, 2, 3, 5, 8, 30, 32, 64, 56, 159, 120, 21, 17, 35, 45, 320).foreach(bitFilter.add)

    if (bitFilter.exists(320)) {
      logger.info("Temp: 320 exists")
    }

    if (!bitFilter.exists(321)) {
      logger.info("Temp: 321 doesn't exist")
    }
    bitFilter.show(100)
    logger.info(s"{bitCount=${bitFilter.bitCount}, k=${bitFilter.getK}," +
      s" size=${bitFilter.getSize / 1024}KB, FPR=${bitFilter.getFPR}}")
  }
}
