package com.wallace.demo.app.utils

import java.util.Random

import scala.collection.{Map, Seq, mutable}

/**
  * Created by 10192057 on 2018/6/22 0022.
  */
object AdminUtils {
  val rand = new Random

  def assignReplicasToBrokersRackUnaware(nPartitions: Int,
                                         replicationFactor: Int,
                                         brokerList: Seq[Int],
                                         fixedStartIndex: Int,
                                         startPartitionId: Int): Map[Int, Seq[Int]] = {
    val ret = mutable.Map[Int, Seq[Int]]()
    val brokerArray = brokerList.toArray
    val startIndex = if (fixedStartIndex >= 0) fixedStartIndex else rand.nextInt(brokerArray.length)
    var currentPartitionId = math.max(0, startPartitionId)
    var nextReplicaShift = if (fixedStartIndex >= 0) fixedStartIndex else rand.nextInt(brokerArray.length)
    for (_ <- 0 until nPartitions) {
      if (currentPartitionId > 0 && currentPartitionId % brokerArray.length == 0)
        nextReplicaShift += 1
      val firstReplicaIndex = (currentPartitionId + startIndex) % brokerArray.length
      val replicaBuffer = mutable.ArrayBuffer(brokerArray(firstReplicaIndex))
      for (j <- 0 until replicationFactor - 1)
        replicaBuffer += brokerArray(replicaIndex(firstReplicaIndex, nextReplicaShift, j, brokerArray.length))
      ret.put(currentPartitionId, replicaBuffer)
      currentPartitionId += 1
    }
    ret
  }

  def replicaIndex(firstReplicaIndex: Int, secondReplicaShift: Int, replicaIndex: Int, nBrokers: Int): Int = {
    val shift = 1 + (secondReplicaShift + replicaIndex) % (nBrokers - 1)
    (firstReplicaIndex + shift) % nBrokers
  }


  def main(args: Array[String]): Unit = {
    val res: Map[Int, Seq[Int]] = assignReplicasToBrokersRackUnaware(150, 2, 10000 to 10045, 0, 0)

    res.toArray.sortBy(_._1).foreach(println)
  }
}
