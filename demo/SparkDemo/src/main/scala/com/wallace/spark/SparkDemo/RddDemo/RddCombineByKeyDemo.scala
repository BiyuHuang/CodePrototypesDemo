package com.wallace.spark.SparkDemo.RddDemo

import com.wallace.common.LogSupport
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by Wallace on 2016/4/27.
  */
object RddCombineByKeyDemo extends LogSupport {


  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("combinByKey")
    val sc = new SparkContext(conf)
    val people = List(("male", "Mobin"), ("male", "Kpop"), ("female", "Lucy"), ("male", "Lufei"), ("female", "Amy"))
    val number = List(1, 2, 3, 4, 5, 6, 7, 8)
    val rdd = sc.parallelize(people)
    val rdd1 = sc.makeRDD(number, 4)
    val combinByKeyRDD = rdd.combineByKey(
      (x: String) => (List(x), 1),
      (peo: (List[String], Int), x: String) => (x :: peo._1, peo._2 + 1),
      (sex1: (List[String], Int), sex2: (List[String], Int)) => (sex1._1 ::: sex2._1, sex1._2 + sex2._2))
    combinByKeyRDD.foreach(println)

    //    log.error(s"######### ${rdd.collect.toList}")
    //    log.error(s"#############################")
    //    log.error(s"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")

    log.error(s"############# ${rdd1.collect.toList} ")
    sc.stop()
  }
}
