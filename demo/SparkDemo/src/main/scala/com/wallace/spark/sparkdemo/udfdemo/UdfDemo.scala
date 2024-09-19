package com.wallace.spark.sparkdemo.udfdemo

import com.wallace.common.CreateSparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.UserDefinedFunction

/**
  * Created by wallace on 2020/3/10.
  */
object UdfDemo extends CreateSparkSession {
  private val spark = createSparkSession("Spark UDF Demo", "local[2]")

  case class Purchase(customer_id: Int, purchase_id: Int, date: String, time: String, tz: String, amount: Double)

  val x: RDD[Purchase] = spark.sparkContext.parallelize(Array(
    Purchase(123, 234, "2007-12-12", "20:50", "UTC", 500.99),
    Purchase(123, 247, "2007-12-12", "15:30", "PST", 300.22),
    Purchase(189, 254, "2007-12-13", "00:50", "EST", 122.19),
    Purchase(187, 299, "2007-12-12", "07:30", "UTC", 524.37)
  ))
  val df: DataFrame = spark.createDataFrame(x)
  df.createOrReplaceTempView("df")

  def main(args: Array[String]): Unit = {
    spark.sparkContext.setLogLevel("WARN")
    val squared: Int => Int = (s: Int) => {
      s * s
    }
    spark.udf.register("square", squared)
    spark.range(1, 20).createOrReplaceTempView("test")
    spark.sql("select id, square(id) as id_squared from test").show(100, truncate = false)


    // TODO 时间转换UDF函数
    def makeDT(date: String, time: String, tz: String) = s"$date $time $tz"
    spark.udf.register("makeDt", makeDT(_: String, _: String, _: String))
    spark.sql("SELECT amount, makeDt(date, time, tz) from df").show(100, truncate = false)

    import org.apache.spark.sql.functions.udf
    val makeDt: UserDefinedFunction = udf(makeDT(_: String, _: String, _: String))
    import spark.implicits._
    df.select($"customer_id", makeDt($"date", $"time", $"tz"), $"amount").show(100, truncate = false)
  }
}
