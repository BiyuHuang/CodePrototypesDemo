package com.wallace.common.timeformat

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, TimeZone}

/**
  * Created by Wallace on 2016/5/6.
  * 时间函数
  */
object TimePara {
  def getYear: Int = {
    val now: Date = new Date()
    val year = now.getYear + 1900
    year
  }

  def getMonth: Int = {
    val now: Date = new Date()
    val month = now.getMonth + 1
    month
  }

  def getDate: Int = {
    val now: Date = new Date()
    val date = now.getDate
    date
  }

  def getHour: Int = {
    val now: Date = new Date()
    val hour = now.getHours
    hour
  }

  def getMinute: Int = {
    val now: Date = new Date()
    val minute = now.getMinutes
    minute
  }

  def getSecond: Int = {
    val now: Date = new Date()
    val second = now.getSeconds
    second
  }

  def getCurrentDate: String = {
    val now: Date = new Date(System.currentTimeMillis())
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val time = dateFormat.format(now)
    time
  }

  def getCurrentTime: String = {
    val now: Date = new Date(System.currentTimeMillis())
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val time = dateFormat.format(now)
    time
  }


  def getDatePartition: String = {
    val now: Date = new Date()
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val time = dateFormat.format(now)
    time
  }

  def getTimePartition: String = {
    val now: Date = new Date()
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss")
    val time = dateFormat.format(now)
    time
  }


  //获取时间戳(毫秒数)
  def getTimeMillis: Long = {
    val tempTime = System.currentTimeMillis()
    tempTime
  }


  def getYesterday: String = {
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    val yesterday = dateFormat.format(cal.getTime)
    yesterday
  }

  def getCurrentWeekStart: String = {
    var period: String = ""
    val cal: Calendar = Calendar.getInstance()
    val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    //获取本周一的日期
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    period = df.format(cal.getTime)
    period
  }

  def getCurrentWeekEnd: String = {
    var period: String = ""
    val cal: Calendar = Calendar.getInstance()
    val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) //这种输出的是上个星期周日的日期，因为国外把周日当成第一天
    cal.add(Calendar.WEEK_OF_YEAR, 1) // 增加一个星期，才是我们中国人的本周日的日期
    period = df.format(cal.getTime)
    period
  }

  def getFirstDayOfMonth: String = {
    var period: String = ""
    val cal: Calendar = Calendar.getInstance()
    val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    cal.set(Calendar.DATE, 1)
    period = df.format(cal.getTime) //本月第一天
    period
  }

  def getEndDayOfMonth: String = {
    var period: String = ""
    val cal: Calendar = Calendar.getInstance()
    val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    cal.set(Calendar.DATE, 1)
    cal.roll(Calendar.DATE, -1)
    period = df.format(cal.getTime) //本月最后一天
    period
  }

  def dateFormat(seconds: String, timeZoneID: String = "GMT"): String = {
    Calendar.getInstance().setTimeZone(TimeZone.getTimeZone(timeZoneID))
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val date: String = sdf.format(new Date(seconds.toLong * 1000 + new Date().getTimezoneOffset * 60 * 1000))
    date
  }

  def timeFormat(time: String): String = {
    val sdf: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss")
    val date: String = sdf.format(new Date(time.toLong * 1000))
    date
  }

  //获取时间差，只能计算当天时间，不支持隔天计算。
  def getCostTime(start_time: String = "22:10:10", end_Time: String = "22:11:14"): String = {
    val df: SimpleDateFormat = new SimpleDateFormat("HH:mm:ss")
    val begin: Date = df.parse(start_time)
    val end: Date = df.parse(end_Time)
    val between: Double = 1.0 * ((end.getTime - begin.getTime) / 1000) //转化成秒
    //val hour: Float = between.toFloat / 3600
    //    val decf: DecimalFormat = new DecimalFormat("#.00")
    //    decf.format(between) //格式化
    between.toString + " s"
  }

  def getCostTime(start_time: Long, end_time: Long): String = {
    val between: Double = ((end_time - start_time) / 1000) * 1.0
    between.toString + " s"
  }

  def getCostTime(start_time: Date, end_time: Date): String = {
    val begin: Long = start_time.getTime
    val end: Long = end_time.getTime
    val cost: Double = 1.0 * ((end - begin) / 1000)
    cost.toString + " s"
  }
}
