package com.wallace.demo.app

import java.io.File
import java.lang.management.ManagementFactory

import com.wallace.demo.app.common.LogSupport

import scala.collection.immutable
import scala.concurrent.duration.TimeUnit
import scala.util.control.Breaks._
import scala.util.control.NonFatal

/**
  * Created by Wallace on 2017/2/24.
  */
object Boot extends LogSupport {
  def convert(time: Long, unit: TimeUnit): Long = unit.convert(time, unit)


  def main(args: Array[String]): Unit = {
    util.Properties.setProp("scala.time", "true")
    var file: Option[File] = None
    try {
      file = Some(new File("./test.csv"))
      log.info(file.get.getPath)
    } catch {
      case NonFatal(e) =>
        log.error(s"Catch Non-Fatal Exception: ${e.getMessage}.")
    } finally {
      if (file.isDefined) {
        file.get.delete()
        log.info("delete file.")
      }
    }
    log.info(s"${func2(4)}")
    log.info("End.")
  }

  def func1(): Unit = {
    log.info(s"${ManagementFactory.getRuntimeMXBean.getName}")
    try {
      util.Properties.setProp("scala.time", "true")
      var a = 1
      val b = 2
      breakable {
        while (true) {
          breakable {
            if (a.equals(b)) {
              a += 1
              log.info("Testing @the first place.")
              break()
            } else {
              a += 1
              log.info("Testing @the second place.")
            }
            log.info("Testing @the third place.")
          }
          if (a == 5) break() else log.info("Testing @the fourth place.")
        }
      }
      log.info("Testing @fifth place.")
    } catch {
      case NonFatal(e) =>
        log.error(s"Catch Non-Fatal Exception: ${e.getMessage}.")
    }
  }

  def func2(in: Int): AnyVal = in match {
    case 1 => 10
    case 2 => 20
    case 3 => 30
    case _ =>
  }
}
