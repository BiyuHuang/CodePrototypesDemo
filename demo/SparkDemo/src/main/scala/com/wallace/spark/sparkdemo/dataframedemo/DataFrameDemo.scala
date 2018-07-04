package com.wallace.spark.sparkdemo.dataframedemo

import com.wallace.common.timeformat.TimePara
import com.wallace.spark.CreateSparkSession
import com.wallace.spark.sparkdemo.dataframedemo.PersonInfo._
import com.wallace.spark.sparkdemo.dataframedemo.SpendingInfo.{Id, Spending, Time}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel

import scala.util.Try

/**
  * Created by Wallace on 2016/6/1.
  * DataFrame Demo
  */

case class Person(NAME: String, AGE: Int, GENDER: String, MARITAL_STATUS: String, HOBBY: String)

case class Customer(Time: String, Id: String, Spending: Int)

case class Record(num: Int, col2: String, col3: String, col4: String, col5: String, index: Int)

object DataFrameDemo extends CreateSparkSession {
  val schema: StructType = StructType(Array(StructField("num", IntegerType, nullable = true),
    StructField("col2", StringType, nullable = true),
    StructField("col3", StringType, nullable = true),
    StructField("col4", StringType, nullable = true),
    StructField("col5", StringType, nullable = true),
    StructField("index", IntegerType, nullable = true)))

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = createSparkSession("DataFrameDemo")
    spark.conf.set("spark.driver.memory", "3g")
    sqlDemo(spark)
    joinDemo(spark)
  }

  private def joinDemo(spark: SparkSession): Unit = {
    //TODO A FullJoin B, A(K,V) B(K,V), A < B  =>  A leftJoin B  +  B.filter(p => !hashSet.contains(p._1))
    val rddA: RDD[(String, Int)] = spark.sparkContext.makeRDD(Array("wallace 2", "Lina 3", "Lucy 4")).map(x => x.split(""" """))
      .map(p => (p(0), p(1).toInt)).reduceByKey(_ + _)
    val rddB: RDD[(String, Int)] = spark.sparkContext.makeRDD(Array("wallace 5", "Lina 2", "Lucy 9", "Sofia 10", "Anna 103", "David 9", "Mary 19"))
      .map(x => x.split(""" """))
      .map(p => (p(0), p(1).toInt)).reduceByKey(_ + _)

    rddB.persist(StorageLevel.MEMORY_ONLY_SER_2)

    val tempRDD = rddA.leftOuterJoin(rddB)
    val hashSet: Set[String] = tempRDD.filter(_._2._2.isDefined).map(_._1).collect().toSet

    val rdd: RDD[(String, Int)] = tempRDD.map {
      p =>
        val temp = p._2._2 match {
          case Some(value) => value
          case None => 0
        }

        val res = temp * p._2._1
        (p._1, res)
    }


    val rddB1: RDD[(String, Int)] = rddB.filter(p => !hashSet.contains(p._1)).map(p => (p._1, p._2 * p._2))

    val res1: Array[(String, Int)] = rdd.collect()
    val res2: Array[(String, Int)] = rddB1.collect()
    res1 ++ res2 foreach println
    println(s"HashSet Size: ${hashSet.size}, HashSet: ${hashSet.mkString("#")}")
  }

  protected def udfFuncDemo(spark: SparkSession): Unit = {
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._
    val rdd: RDD[String] = sc.textFile("./demo/SparkDemo/src/main/resources/sample_1.csv")
    val srcDF: DataFrame = rdd.map(_.split(",", -1)).map {
      col =>
        Record(col(0).toInt, col(1), col(2), col(3), col(4), col(5).toInt)
    }.toDF
    srcDF.show(5)
    val res: RDD[Row] = flatMapFunc(srcDF)
    res.collect.foreach {
      row =>
        log.info(s"#### ${row.mkString(",")}")
    }
    val resDF = spark.createDataFrame(res, schema)
    resDF.show()
  }

  protected def sqlDemo(spark: SparkSession): Unit = {
    val sc: SparkContext = spark.sparkContext
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
    res1.map(x => x.getInt(1)).rdd.take(1)
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
    //res5.write.format("csv").mode(SaveMode.Overwrite).save("/")
    //res5.write.mode(SaveMode.Append).parquet("/")
  }

  protected def padto(ls: Array[String], columnNum: Int = 5): Array[String] = if (ls.length > columnNum) ls.dropRight(ls.length - columnNum) else ls.padTo(columnNum, "")

  protected def getSparkTableLocation(spark: SparkSession, tableName: String): String = {
    val resDF: DataFrame = spark.sql(s"DESC FORMATTED $tableName")
    resDF.filter(resDF.col("col_name") === "Location:").rdd.map(x => x.getString(1)).take(1).head
  }

  protected def explodeFunc(srcDF: DataFrame): DataFrame = {
    val temp: DataFrame = srcDF.filter(srcDF.col("num") > 0).explode(srcDF.col("col2"), srcDF.col("col3"), srcDF.col("col4"), srcDF.col("col5")) {
      row: Row =>
        val col2 = row.getString(0).split("\\$", -1)
        val col3 = row.getString(1).split("\\$", -1)
        val col4 = row.getString(2).split("\\$", -1)
        val col5 = row.getString(3).split("\\$", -1)
        col5.indices.map(i => (i, col2(i), col3(i), col4(i), col5(i)))
    }.toDF("num", "col2", "col3", "col4", "col5", "index", "lineNum", "col_2", "col_3", "col_4", "col_5")
    temp
  }

  protected def flatMapFunc(srcDF: DataFrame): RDD[Row] = {
    srcDF.rdd.flatMap {
      row =>
        val num: Int = Try(row.getInt(0)).getOrElse(-1)
        val res: Array[_ >: Seq[Nothing] <: Seq[Any]] = num match {
          case 0 => Array(Seq.empty)
          case _ =>
            val col2 = Try(row.getString(1)).getOrElse("").split("\\$", -1)
            val col3 = Try(row.getString(2)).getOrElse("").split("\\$", -1)
            val col4 = Try(row.getString(3)).getOrElse("").split("\\$", -1)
            val col5 = Try(row.getString(4)).getOrElse("").split("\\$", -1)
            val index: Array[Int] = col2.zipWithIndex.map(_._2)
            if (col2.length != num | col3.length != num | col4.size != num) {
              Array(Seq.empty)
            } else {
              index.map {
                i =>
                  if (col5(i) == "") {
                    Seq.empty
                  } else {
                    Seq(i, col2(i), col3(i), col4(i), col5(i), row.getInt(5))
                  }
              }
            }
        }

        res.filter(_.nonEmpty).map {
          x =>
            Row.fromSeq(x)
        }
    }
  }
}
