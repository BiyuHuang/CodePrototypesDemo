package com.wallace.spark.DataFrameDemo

import com.wallace.spark.DataFrameDemo.PersonInfo._
import com.wallace.spark.DataFrameDemo.SpendingInfo._
import com.wallace.spark.common.LogSupport
import com.wallace.spark.common.TimeFormat.TimePara
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
  * Created by Wallace on 2016/6/1.
  * DataFrame Demo
  */

case class Person(NAME: String, AGE: Int, GENDER: String, MARITAL_STATUS: String, HOBBY: String)

case class Customer(Time: String, Id: String, Spending: Int)

object DataFrameDemo extends LogSupport {

  def padto(ls: Array[String], columnNum: Int = 5): Array[String] = if (ls.length > columnNum) ls.dropRight(ls.length - columnNum) else ls.padTo(columnNum, "")

  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("DfDemo")
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    /**
      * Personal Information
      */
    val data: List[String] = List("Wallace Huang,26,Male,Single,Basketball", "Bruce Wade,24,Male,Double,Book", "Lucy Adan,21,Female,Single,Computer", "Lina Anna,27,Female,Double,Shopping", "David Han,30,Male,Double,Reading")
    val personData = sc.makeRDD(data, 2)
    //    personData.foreach(column => println(column))
    //    log.error("#########" + personData.collect.toList)
    val personDF = personData.map(line => padto(line.split(",", -1)))
      .map(column => Person(column(NAME.id),
        column(AGE.id).toInt,
        column(GENDER.id),
        column(MARITAL_STATUS.id),
        column(HOBBY.id))).toDF()

    personDF.registerTempTable("person_info")
    val res1: DataFrame = sqlContext.sql(s"SELECT * FROM person_info")
    res1.show()

    /**
      * Purchase Something
      */
    val purchase: List[String] = List(s"${TimePara.getCurrentDate},Wallace Huang,1900", "2016-06-01 08:01:00,Bruce Wade,",
      "2016-05-31 10:01:00,Lucy Adan,1056", "2016-05-23 09:00:00,Lina Anna,912", "2016-04-23 19:01:32,David Han,182")
    val purchaseRdd = sc.makeRDD(purchase, 2)
    val schema: StructType = StructType(Array(StructField("Time", StringType, nullable = true), StructField("Id", StringType, nullable = true), StructField("Spending", IntegerType, nullable = true)))
    val rowPurchaseRdd: RDD[Row] = purchaseRdd.map(_.split(",", -1)).map(column => Row(column(Time.id), column(Id.id), Try(column(Spending.id).toInt).getOrElse(0)))
    val purchaseDF = sqlContext.createDataFrame(rowPurchaseRdd, schema)
    //    val purchaseDF = purchaseRdd.map(line => padto(line.split(",", -1)))
    //      .map(column => Customer(column(Time.id), column(Id.id), column(Spending.id).toInt)).toDF
    purchaseDF.registerTempTable("spending_info")
    val res2: DataFrame = sqlContext.sql(s"Select Time,Id,Spending From spending_info Order by Time")
    res2.show()

    /**
      * 两表联合查询
      */
    val res3: DataFrame = res1.join(res2, res1.col("NAME").equalTo(res2.col("Id"))).orderBy("Time").where("Spending != 0 ")
    res3.show()
    //    res3.printSchema()
    
    val res4: DataFrame = res3.filter(res3.col("Time") >= "2016-05-23 09:00:00")
    res4.show()
  }
}
