//package com.wallace.spark.sparkdemo.rdddemo
//
//import breeze.linalg.DenseMatrix
//import com.wallace.common.{CreateSparkSession, Using}
//import nak.cluster.{DBSCAN, GDBSCAN, Kmeans}
//import org.apache.spark.rdd.RDD
//
///**
//  * Created by wallace on 2019/8/29.
//  */
//object DBScanDemo extends CreateSparkSession with Using {
//  def main(args: Array[String]): Unit = {
//    usingSpark(createSparkSession("DBScanDemo")) {
//      spark =>
//        spark.sparkContext.setLogLevel("WARN")
//        val testData: RDD[(Int, DenseMatrix[Double])] = spark.sparkContext.parallelize(Array((15474,
//          DenseMatrix((40.8379525833, -73.70209875),
//            (40.6997066969, -73.8085234163),
//            (40.6997066968, -73.8085234162),
//            (40.6997066967, -73.8085234161),
//            (40.6997066966, -73.8085234160),
//            (40.7484436586, -73.9857316017),
//            (40.7484436585, -73.9857316016),
//            (40.7484436584, -73.9857316015),
//            (40.7484436583, -73.9857316017),
//            (40.750613797, -73.993434906),
//            (40.750613796, -73.993434902),
//            (40.750613795, -73.993434903),
//            (40.750613794, -73.993434905),
//            (40.750613793, -73.993434908),
//            (40.750613792, -73.993434905)))))
//
//        testData.mapValues(dbScan).collect.foreach(println)
//
//    }
//  }
//
//  def dbScan(v: breeze.linalg.DenseMatrix[Double]): Seq[GDBSCAN.Cluster[Double]] = {
//    val gdbscan: GDBSCAN[Double] = new GDBSCAN(
//      DBSCAN.getNeighbours(epsilon = 0.001, distance = Kmeans.euclideanDistance),
//      DBSCAN.isCorePoint(minPoints = 3)
//    )
//    gdbscan.cluster(v)
//  }
//}
//
