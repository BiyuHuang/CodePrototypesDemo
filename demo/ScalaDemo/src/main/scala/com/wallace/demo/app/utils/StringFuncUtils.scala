package com.wallace.demo.app.utils

import java.util
import com.wallace.demo.app.common.Using
import com.wallace.demo.app.utils.stringutils.StringUtils

import java.util.concurrent.ForkJoinPool
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.util.control.Breaks.{break, breakable}
import scala.util.{Properties, Try}

/**
  * Created by Wallace on 2017/1/11.
  */
object StringFuncUtils extends Using {
  private var _uniqueIndex: Long = updateUniqueIndex(((System.currentTimeMillis() - 946656000000L) / 1000) % Int.MaxValue)
  private val _maxParallelism: Int = Runtime.getRuntime.availableProcessors()
  private val _curParallelism: Int = Math.min(_maxParallelism, 5)


  private val srcColumnsFields: Map[String, Int] = "time,key1,key2,lon,lat,mark,col1,col2,col3,col4,col5,col6,col7,col8,col9,col10,col11,col12,col13,col14,col15,col16,col17,col18,col19,col20,col21,col22,col23,col24,col25,col26,col27,col28,col29,col30,col31,col32,col33,col34,col35,col36,col37,col38,col39,col40,col41,col42,col43,col44,col45,col46,col47,col48,col49,col50".split(",", -1).map(_.trim).zipWithIndex.toMap
  private val tgtColumnsFields: Array[String] = "time,key1,key2,mark,col1,lon,lat,col6,col7,col8,col9,col10,col11,col12,col13,col14,col15,col16,col17,col18,col19,col20,col21,col22,col23,col24,col25,col26,col27,col28,col29,col30,col31,col32,col33,col34,col35,col36,col37,col38,col39,col40,col41,col42,col43,col44,col45,col46,col47,col48,col49,col50".split(",", -1)
  private val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]
  m_SrcColumnsFields.putAll(srcColumnsFields.asJava)

  private val m_TgtColumnsFields: util.ArrayList[String] = new util.ArrayList[String]()
  tgtColumnsFields.foreach(m_TgtColumnsFields.add)

  def getMaxSubStrLen(str: String): Int = {
    val strHashCode: Seq[Int] = str.intern().map(_.hashCode())
    val tempRes: Seq[(Int, Int)] = (0 until strHashCode.length - 1).map {
      i =>
        var asc_len = 1
        var desc_len = 1
        (i until strHashCode.length - 1).foreach {
          j =>
            breakable {
              val cur = strHashCode(j)
              val next = strHashCode(j + 1)
              if (next == cur + 1) {
                asc_len += 1
              } else {
                break()
              }
            }
        }

        (i until strHashCode.length - 1).foreach {
          j =>
            breakable {
              val cur = strHashCode(j)
              val next = strHashCode(j + 1)
              if (next == cur - 1) {
                desc_len += 1
              } else {
                break()
              }
            }
        }
        (asc_len, desc_len)
    }

    Math.max(tempRes.map(_._1).max, tempRes.map(_._2).max)
  }

  private val splitColumnsFields: Map[String, (String, Int)] = {
    val temp: Array[String] = "col1 = extra_col1,extra_col2".split("=", -1).map(_.trim)
    val keyWithIndex = temp.last.split(",").zipWithIndex
    val value = temp.head

    keyWithIndex.flatMap {
      ki =>
        val v: (String, Int) = (value, ki._2)
        Map(ki._1 -> v)
    }.toMap
  }
  private val m_SplitColumnsFields: util.HashMap[String, (String, Int)] = new util.HashMap[String, (String, Int)]()
  m_SplitColumnsFields.putAll(splitColumnsFields.asJava)

  private val concatColumnsFields: Map[String, Array[String]] = {
    val temp: Array[String] = "col1,col2 = concat_col1".split("=", -1).map(_.trim)
    val key = temp.last
    val value = temp.head.split(",")

    Map(key -> value)
  }

  private val m_ConcatColumnsFields: util.HashMap[String, Array[String]] = new util.HashMap[String, Array[String]]()
  m_ConcatColumnsFields.putAll(concatColumnsFields.asJava)

  def extractFieldsJava(src: String, defaultSep: String = ","): String = {
    //TODO  1. 表头信息/索引
    //TODO  2. 输出信息/索引
    //TODO  3. 解析逻辑(包含索引、字段)
    //TODO     提取列      5人天
    //TODO     字段拆分与拼接(输入字段(索引) => 输出字段(索引))  3人天
    //TODO     字符串替换(可配置： replace_string) 1人天
    //TODO     取子串(可配置：[0，length])     1人天
    //TODO     单位转换(支持简单四则运算、求余、进制转换)  3人天
    //TODO     公共字段拼接(如文件名中的时间戳)
    //TODO     时间戳转换(*******???)
    /**
      * Example
      * InPut: 2018-4-8 17:19:19,666666,1,109.01,32.34,true,1,2,3,4,5,6
      * OutPut: 2018-4-8 17:19:19,666666,1,true,1,109.01,32.34
      */
    val res = new StringBuilder
    val len = tgtColumnsFields.length - 1
    val data: Array[String] = if (src.contains("\"")) src.split(defaultSep, -1) else splitString(src, ",", "\"")
    val cachedData: util.HashMap[String, Array[String]] = new util.HashMap[String, Array[String]]()
    tgtColumnsFields.indices.foreach {
      i =>
        val key = tgtColumnsFields(i)
        val value: String = if (m_SplitColumnsFields.containsKey(key)) {
          val keyWithIndex: (String, Int) = m_SplitColumnsFields.get(key)
          if (!cachedData.containsKey(keyWithIndex._1)) {
            cachedData.put(keyWithIndex._1, data(m_SrcColumnsFields.get(keyWithIndex._1)).split("$"))
          }
          cachedData.get(keyWithIndex._1)(keyWithIndex._2)
        } else if (m_ConcatColumnsFields.containsKey(key)) {
          val tempData: Array[String] = m_ConcatColumnsFields.get(key).map(
            k =>
              data(m_SrcColumnsFields.get(k))
          )
          tempData.mkString("#")
        } else {
          data(m_SrcColumnsFields.get(key))
        }
        if (i < len) res.append(value).append(",") else res.append(value)
    }

    cachedData.clear()
    res.toString()
  }

  def extractFieldsScala(src: String, defaultSep: String = ","): String = {
    val res = new StringBuilder
    val len = tgtColumnsFields.length - 1
    val data: Array[String] = if (src.contains("\"")) src.split(defaultSep, -1) else splitString(src, ",", "\"")
    val cachedData: util.HashMap[String, Array[String]] = new util.HashMap[String, Array[String]]()
    tgtColumnsFields.indices.foreach {
      i =>
        val key = tgtColumnsFields(i)
        val value: String = if (splitColumnsFields.contains(key)) {
          val keyWithIndex: (String, Int) = splitColumnsFields(key)
          if (!cachedData.containsKey(key)) {
            cachedData.put(key, data(m_SrcColumnsFields.getOrDefault(keyWithIndex._1, keyWithIndex._2)).split("$"))
          }
          cachedData.get(key)(keyWithIndex._2)
        } else if (concatColumnsFields.contains(key)) {
          val tempData: Array[String] = concatColumnsFields(key).map(
            k =>
              data(m_SrcColumnsFields.get(k))
          )
          tempData.mkString("#")
        } else {
          data(srcColumnsFields(key))
        }
        if (i < len) res.append(value).append(",") else res.append(value)
    }

    cachedData.clear()
    res.toString()
  }

  def splitString(str: String, fieldSeparator: String, specialChar: String): Array[String] = {
    //TODO There is some error
    val resultArr = new ArrayBuffer[String]()
    var temp = str
    str match {
      case v if v.isEmpty => Array("")
      case _ =>
        while (temp.nonEmpty) {
          var nPos: Int = temp.indexOf(fieldSeparator)
          if (nPos >= 0) {
            var sSub = temp.substring(0, nPos)
            if (sSub.indexOf(specialChar) != -1) {
              if (sSub.indexOf(specialChar) == sSub.lastIndexOf(specialChar)) {
                temp = temp.substring(nPos, temp.length)
                nPos = temp.indexOf(specialChar)
                if (nPos != -1) {
                  sSub = sSub + temp.substring(0, nPos)
                }
                sSub = sSub.replaceAll(specialChar, "")
                resultArr.append(sSub)
                temp = if (nPos + 2 < temp.length) temp.substring(nPos + 2, temp.length) else ""
              } else {
                temp = temp.substring(nPos + 1, temp.length)
                sSub = sSub.replaceAll(specialChar, "")
                resultArr.append(sSub)
              }
            } else {
              resultArr.append(sSub)
              temp = temp.substring(nPos + 1, temp.length)
            }
          } else {
            resultArr.append(temp)
            temp = temp.substring(temp.length)
          }
        }
        resultArr.result().toArray
    }
  }

  def subStr(str: String): String = {
    val str1 = "/data2/hadoop/vmaxrun/drs/post_telecom_pm_cm/hdfs/zxvmax/telecom/temp/lte/rawdata/itg_pm_lte_enb_h_enb_d/p_provincecode=510000/"
    str.substring(str1.indexOf("post_"), str1.length)
  }

  def stringConventToBytes(str: String, charsetName: String = "UTF-8"): Array[Byte] = Try(str.getBytes(charsetName)).getOrElse(Array.emptyByteArray)

  def bytesCoventToString(bytes: Array[Byte], charsetName: String = "UTF-8"): String = new String(bytes, charsetName)

  def main(args: Array[String]): Unit = {
    val pool = new ForkJoinTaskSupport(new ForkJoinPool(_curParallelism))
    Properties.setProp("scala.time", "true")
    runtimeDuration({
      _uniqueIndex = updateUniqueIndex(_uniqueIndex)
      logger.info(s"UniqueIndex: ${_uniqueIndex}")
    }, 10)

    logger.info(formatString("16.1"))
    val str0 = """1,2,3,4,"a=1,b=2,c=3","e=1.2,f=32.1,g=1.3",7,8,9"""
    val str1 = """1,2,3,"4,"a=1,b=2,c=3",10,11,12,13,"e=1.2,f=32.1,g=1.3",7,8,9"""
    val str2 = """1,2,3,4","a=1,b=2,c=3",10,11,12,13,"e=1.2,f=32.1,g=1.3",7,8,9"""
    val vRes: java.util.Vector[String] = new java.util.Vector[String]()
    FuncUtil.GetSplitString(vRes, str0, ",")
    val input = Array(str0, str1, str2)
    input.par.tasksupport = pool
    input.par.foreach {
      str =>
        val threadName = Thread.currentThread().getName
        logger.info(s"$threadName: $str")
        val res: Array[String] = splitString(str, ",", "\"")
        synchronized {
          res.foreach {
            elem =>
              logger.info(s"$threadName: $elem")
          }
        }
    }
  }

  def countKeyWord(in: String, sep: String): Map[String, Int] = {
    val res: Map[String, Array[(String, Int)]] = in.split(sep).map(x => (x, 1)).groupBy(x => x._1)
    res.map(x => (x._1, x._2.length))

    //    val res: Map[String, Int] = in.split(sep).map(x => (x, 1)).reduceLeft {
    //      (r, A) =>
    //        if(r.
    //    }
  }

  def updateUniqueIndex(initIndex: Long): Long = synchronized {
    if (initIndex > Int.MaxValue) 0L else initIndex + 1
  }

  def formatString(s: String): String = {
    //TODO scala字符串格式化-StringLike.format()
    s match {
      case "" => ""
      case _: String =>
        val a = "%1$s-%2$s-%3$s".format("scala", "StringLike", "format")
        val b = "%d%%".format(s.toDouble.toInt)
        val c = "%8.3f".format(s.toDouble)
        val d = "%08.3f".format(s.toDouble)
        val e = "%09d".format(s.toDouble.toInt)
        val f = "%.2f".format(s.toDouble)
        val g = "%.0f".format(s.toDouble)
        val h = f"${s.toDouble}%.2f"

        s"""
           |$a
           |$b
           |$c
           |$d
           |$e
           |$f
           |$g
           |$h
         """.stripMargin
      case _ =>
        ""
    }
  }

  //TODO Convert "25525511135" to “255.255.11.135”, “255.255.111.35”
  def convertStrToFixedFormat(str: String): List[String] = {
    StringUtils.restoreIpAddresses(str).asScala.toList
  }
}
