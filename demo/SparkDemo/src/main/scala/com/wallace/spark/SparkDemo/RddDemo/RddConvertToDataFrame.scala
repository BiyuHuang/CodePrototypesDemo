package com.wallace.spark.SparkDemo.RddDemo

import com.wallace.common.LogSupport
import com.wallace.snmp.SnmpPDUAnalysis
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.Try

/**
  * Created by huangbiyu on 16-5-15.  @ Spark 1.6
  * Rdd Convert to DataFrame
  * Update by Wallace Huang on 2016-10-30 10:36:35  @ Spark2.0
  */
object RddConvertToDataFrame extends App with LogSupport {
  val warehouseLocation = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/" + "spark-warehouse"
  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("RddConvertToDataFrame")
    .config("spark.sql.warehouse.dir", warehouseLocation)
    //.enableHiveSupport()
    .getOrCreate()

  val sc = spark.sparkContext

  // 为了支持RDD到DataFrame/DataSet的隐式转换
  import spark.implicits._

  // 定义一个case class.
  // 注意：Scala 2.10的case class最多支持22个字段，要绕过这一限制，
  // 你可以使用自定义class，并实现Product接口。当然，你也可以改用编程方式定义schema
  case class Person(name: String, age: Int)

  val schemaString = "name age"
  val fields = schemaString.split(" ").map(fieldName => StructField(fieldName, StringType, nullable = true))
  val schema = StructType(fields)

  // 创建一个包含Person对象的RDD，并将其注册成table
  val resRdd = sc.textFile("demo/SparkDemo/data/test.txt").map(_.split(",")).map(attributes => Person(attributes(0), attributes(1).trim.toInt))
  val peopleDF = spark.createDataset(resRdd) //.createDataFrame(resRdd, schema)
  peopleDF.show(3)
  peopleDF.createOrReplaceTempView("people")

  // spark.sql方法可以直接执行SQL语句   WHERE age >= 13 AND age <= 19
  val teenagers = spark.sql("SELECT name, age FROM people WHERE age >= 13 AND age <= 19")
  log.error("##############" + teenagers.count())
  val startTime = System.currentTimeMillis()
  teenagers.foreachPartition {
    part =>
      val line = part.map(ls => s"""('${ls.toString.drop(1).dropRight(1)})""".replaceFirst(",", "',")).reduce(_ + "," + _)
      log.error("#############" + line)
  }
  val endTime = System.currentTimeMillis()
  log.error(s"Cost time : ${endTime - startTime} ms.")

  // 查询结果中每行的字段可以按字段索引访问:
  teenagers.map(t => "Name: " + t(0)).show(3)
  // 或者按字段名访问:
  teenagers.map(t => "Name: " + t.getAs[String]("name") + "     Age: " + t.getAs[Int]("age")).show(3)


  /**
    * Snmp PDU Analysis
    */
  val pduData = new SnmpPDUAnalysis("demo/SparkDemo/data/currentAlarmTable.csv")
  val resPDUData = pduData.process()

  case class alarmTableDataFrame(col_1: String, col_2: Int, col_3: String, col_4: String)

  val alarmTableRdd = sc.makeRDD(resPDUData, 5)
  val alarmTableDS: Dataset[alarmTableDataFrame] = alarmTableRdd.map(row => row.split("\\|", -1)).map {
    row =>
      alarmTableDataFrame(Try(row(0)).getOrElse(""), Try(row(1).toInt).getOrElse(-1), Try(row(2)).getOrElse(""), Try(row(3)).getOrElse(""))
  }.toDS()

  alarmTableDS.show(3)

  spark.stop()
}
