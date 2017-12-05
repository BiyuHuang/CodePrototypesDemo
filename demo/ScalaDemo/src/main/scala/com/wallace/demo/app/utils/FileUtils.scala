package com.wallace.demo.app.utils

import java.io._
import java.nio.charset.Charset
import java.util.zip.{ZipEntry, ZipFile, ZipInputStream}

import com.wallace.demo.app.common.LogSupport

import scala.util.control.NonFatal


object FileUtils extends LogSupport {

  def main(args: Array[String]): Unit = {
    // TODO READ GZ FILE
    //    val file: File = new File("./demo/ScalaDemo/src/main/resources/AH_RM_20170926_all_all-cm_lte_cel-20170926000000-20170927000000-v2.0-20170926112500-001.csv.gz")
    //    val ins: FileInputStream = new FileInputStream(file)
    //    val gs: GZIPInputStream = new GZIPInputStream(ins)
    //    val br = new BufferedReader(new InputStreamReader(gs, "GBK"))
    //    //val sc: Scanner = new Scanner(gs, "GBK")
    //
    //    //    while (sc.hasNextLine) {
    //    //      val oneLine = sc.nextLine().replaceAll("null", "")
    //    //      log.info(s"$oneLine")
    //    //    }
    //
    //    while (br.ready()) {
    //      val oneLine = br.readLine().replaceAll("null", "")
    //      log.info(s"$oneLine")
    //    }
    //

    readZipFile("./demo/ScalaDemo/src/main/resources/CDT_ZTE_V3.5_963847_20171201180000.zip")
  }

  def readZipFile(fileName: String): Unit = {
    try {
      val zipFile: ZipFile = new ZipFile(new File(fileName), Charset.forName("GBK"))
      val bis: BufferedInputStream = new BufferedInputStream(new FileInputStream(fileName))
      val zis: ZipInputStream = new ZipInputStream(bis)
      var entry: Option[ZipEntry] = None
      entry = Some(zis.getNextEntry)
      if (entry.isEmpty || entry.get.isDirectory) {

      } else {
        val size = entry.get.getSize
        if (size > 0) {
          val br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry.get)))
          var cnt: Long = 1
          while (br.ready() && (cnt <= size)) {
            val line = br.readLine()
            log.info(s"$cnt: $line")
            cnt += 1
          }
          br.close()
        }
      }
    } catch {
      case NonFatal(e) =>
        log.error(s"Failed to handle $fileName: ${e.printStackTrace()}")
    }
  }
}
