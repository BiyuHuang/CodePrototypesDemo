package com.wallace.spark.sparkdemo.dataframedemo

import com.wallace.common.LogSupport
import com.wallace.common.timeformat.TimePara
import com.wallace.spark.sparkdemo.dataframedemo.PersonInfo._
import com.wallace.spark.sparkdemo.dataframedemo.SpendingInfo.{Id, Spending, Time}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

import scala.util.Try

/**
  * Created by Wallace on 2016/6/1.
  * DataFrame Demo
  */

case class Person(NAME: String, AGE: Int, GENDER: String, MARITAL_STATUS: String, HOBBY: String)

case class Customer(Time: String, Id: String, Spending: Int)

object DataFrameDemo extends LogSupport {

  def main(args: Array[String]): Unit = {
    val warehouseLocation = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/" + "spark-warehouse"
    val spark: SparkSession = SparkSession
      .builder().master("local[*]").appName("RddConvertToDataFrame").config("spark.sql.warehouse.dir", warehouseLocation)
      .config("spark.driver.memory", "3g")
      //.enableHiveSupport()
      .getOrCreate()
    val sc = spark.sparkContext
    import spark.implicits._
    /**
      * Personal Information
      */
    val data: Array[String] = Array("Wallace Huang,26,Male,Single,Basketball", "Wallace Huang,26,Male,Single,Music", "Bruce Wade,24,Male,Married,Book", "Lucy Adan,21,Female,Single,Computer", "Lina Anna,27,Female,Married,Shopping", "David Han,30,Male,Married,Reading")
    val personData: RDD[String] = sc.makeRDD(data, 2)
    val personDS: Dataset[Person] = personData.map(line => padto(line.split(",", -1)))
      .map(column => Person(column(NAME.id),
        column(AGE.id).toInt,
        column(GENDER.id),
        column(MARITAL_STATUS.id),
        column(HOBBY.id))).toDS()

    personDS.createOrReplaceTempView("person_info")
    val res1: DataFrame = spark.sql(s"SELECT * FROM person_info")

    res1.map(x => x.getString(1)).rdd.take(1)
    res1.show(3)
    /**
      * Purchase Something
      */
    val purchase: Array[String] = Array(s"${TimePara.getCurrentDate},Wallace Huang,1900", "2017-04-01 09:01:00,Wallace Huang,1900", "2016-06-01 08:01:00,Bruce Wade,",
      "2016-05-31 10:01:00,Lucy Adan,1056", "2016-05-23 09:00:00,Lina Anna,912", "2016-04-23 19:01:32,David Han,182")
    val purchaseRdd = sc.makeRDD(purchase, 2)
    val schema: StructType = StructType(Array(StructField("Time", StringType, nullable = true), StructField("Id", StringType, nullable = true), StructField("Spending", IntegerType, nullable = true)))
    val rowPurchaseRdd: RDD[Row] = purchaseRdd.map(_.split(",", -1)).map(column => Row(column(Time.id), column(Id.id), Try(column(Spending.id).toInt).getOrElse(0)))
    val purchaseDF = spark.createDataFrame(rowPurchaseRdd, schema)
    purchaseDF.createOrReplaceTempView("spending_info")
    val res2: DataFrame = spark.sql(s"Select ROW_NUMBER() OVER(Partition by ID Order by Time Desc) as Row_ID,Time,Id,Spending From spending_info")
    res2.show(3)
    /**
      * 两表联合查询
      */
    val res3: DataFrame = res1.join(res2, res1.col("NAME") === res2.col("Id")).orderBy("Time").where("Spending <> 0 ")
    res3.show(3)
    val res4: DataFrame = res3.filter(res3.col("Time") >= "2016-05-23 09:00:00")
    res4.show(3)
    /** select */
    val res5: DataFrame = res4.select("NAME", "Id", "Time", "Spending")
    res5.show(3)
    //    res5.write.format("com.databricks.spark.csv").mode(SaveMode.Overwrite).save("./temp/")
    //    res5.write.format("csv").mode(SaveMode.Overwrite).save("/")
  }

  protected def padto(ls: Array[String], columnNum: Int = 5): Array[String] = if (ls.length > columnNum) ls.dropRight(ls.length - columnNum) else ls.padTo(columnNum, "")

  protected def getSparkTableLocation(spark: SparkSession, tableName: String): String = {
    val resDF: DataFrame = spark.sql(s"DESC FORMATTED $tableName")
    resDF.filter(resDF.col("col_name") === "Location:").map(x => x.getString(1)).rdd.take(1).head
  }
}
