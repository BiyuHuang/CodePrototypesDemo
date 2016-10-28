package com.wallace.spark.SparkDemo

import com.walace.demo.EncodingParser
import com.wallace.snmp.SnmpPDUAnalysis
import com.wallace.spark.common.LogSupport
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
  * Created by huangbiyu on 16-5-15.
  * Rdd Convert to DataFrame
  */
object RddConvertToDataFrame extends App with LogSupport {

  val conf = new SparkConf().setMaster("local[*]").setAppName("RddConvertToDataFrame")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  // 为了支持RDD到DataFrame的隐式转换
  import sqlContext.implicits._

  //  // 定义一个case class.
  //  // 注意：Scala 2.10的case class最多支持22个字段，要绕过这一限制，
  //  // 你可以使用自定义class，并实现Product接口。当然，你也可以改用编程方式定义schema
  //  case class Person(name: String, age: Int)
  //
  //  // 创建一个包含Person对象的RDD，并将其注册成table
  //  val people = sc.textFile("./data/test.txt").map(_.split(",")).map(p => Person(p(0), p(1).trim.toInt)).toDF()
  //  people.registerTempTable("people")
  //  // sqlContext.sql方法可以直接执行SQL语句   WHERE age >= 13 AND age <= 19
  //  val teenagers: DataFrame = sqlContext.sql("SELECT name, age FROM people WHERE age >= 13 AND age <= 19").toDF()
  //  // SQL查询的返回结果是一个DataFrame，且能够支持所有常见的RDD算子
  //
  //  println("##############" + teenagers.count())
  //  val startTime = System.currentTimeMillis()
  //  teenagers.foreachPartition {
  //    part =>
  //      val line = part.map(ls => s"""('${ls.toString.drop(1).dropRight(1)})""".replaceFirst(",", "',")).reduce(_ + "," + _)
  //      println("#############" + line)
  //  }
  //  val endTime = System.currentTimeMillis()
  //  println(s"Cost time : ${endTime - startTime} ms.")
  //  // 查询结果中每行的字段可以按字段索引访问:
  //  //teenagers.map(t => "Name: " + t(0)).collect().foreach(println)
  //  // 或者按字段名访问:
  //  //teenagers.map(t => "Name: " + t.getAs[String]("name")).collect().foreach(println)
  //  // row.getValuesMap[T] 会一次性返回多列，并以Map[String, T]为返回结果类型
  //  //teenagers.map(_.getValuesMap[Any](List("name", "age"))).collect().foreach(println)
  //  // 返回结果: Map("name" -> "Justin", "age" -> 19)


  val tempData = new SnmpPDUAnalysis

  case class alarmTableDataFrame(col_1: String, col_2: Int, col_3: String, col_4: String)

  val alarmTableRdd = sc.makeRDD(tempData.process(), 5)
  val alarmTableDF = alarmTableRdd.map(row => row.split("\\|", -1)).map {
    row =>
      alarmTableDataFrame(Try(row(0)).getOrElse(""), Try(row(1).toInt).getOrElse(-1), Try(row(2)).getOrElse(""), Try(row(3)).getOrElse(""))
  }.toDF()
  alarmTableDF.show(27)
}
