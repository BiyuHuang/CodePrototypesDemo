package com.wallace.demo.app.utils

import java.io._
import java.nio.charset.Charset
import java.util.zip.{GZIPInputStream, ZipFile, ZipInputStream}
import javax.xml.parsers.{SAXParser, SAXParserFactory}

import com.wallace.demo.app.common.Using
import com.wallace.demo.app.parsexml.MROSax
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.xml.sax.helpers.DefaultHandler

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object FileUtils extends Using {
  private val factory: SAXParserFactory = SAXParserFactory.newInstance()
  private var cnt: Int = 0

  def main(args: Array[String]): Unit = {
    //    // TODO READ GZ FILE
    //    val costTime1 = runtimeDuration(readGZFile("./demo/ScalaDemo/src/main/resources/AH_RM_20170926_all_all-cm_lte_cel-20170926000000-20170927000000-v2.0-20170926112500-001.csv.gz"))
    //    log.info(s"CostTime1: $costTime1 ms.")
    //
    //    // TODO READ ZIP FILE
    //    val costTime2 = runtimeDuration(readZipFile("./demo/ScalaDemo/src/main/resources/CDT_ZTE_V3.5_963847_20171201180000.zip"))
    //    log.info(s"CostTime2: $costTime2 ms.")

    // TODO READ TAR.GZ FILE
    val costTime3 = runtimeDuration {
      readTarGZFile("./demo/ScalaDemo/src/main/resources/HW_HN_OMC1-mr-134.175.57.16-20170921043000-20170921044500-20170921051502-001.tar.gz")
    }
    log.info(s"CostTime3: $costTime3 ms.")
  }

  private def readTarGZFile(fileName: String): Unit = {
    cnt = 0
    usingWithErrMsg(new FileInputStream(fileName), s"Failed to get input stream for $fileName") {
      fin =>
        using(new GzipCompressorInputStream(fin)) {
          inputStream =>
            using(new TarArchiveInputStream(inputStream, "UTF-8")) {
              tarInput =>
                while (tarInput.canReadEntryData(tarInput.getNextTarEntry)) {
                  processSingleEntry(tarInput, fileName.split("/").last)
                }
            }
        }
    }
  }

  def processSingleEntry(tarInput: TarArchiveInputStream, fileName: String): Unit = {
    val entry = tarInput.getCurrentEntry
    if (!entry.isDirectory) {
      val size: Long = entry.getSize
      val entryName: String = entry.getName

      //log.info(s"[$cnt]$entryName => $size.")
      assert(size < Int.MaxValue, s"$entryName's size is too long: $size > ${Int.MaxValue}.")
      val context = new Array[Byte](size.toInt)
      var offset = 0
      while (tarInput.available() > 0) {
        offset += tarInput.read(context, offset, 40960)
      }
      entryName.toUpperCase match {
        case v if v.contains("_MRS_") =>
        case v if v.contains("_MRE_") =>
        case v if v.contains("_MRO_") =>

          cnt += 1
          using(new GZIPInputStream(new ByteArrayInputStream(context))) {
            xmlInputStream =>
              val handle: MROSax = new MROSax
              val saxParser: SAXParser = SAXParserFactory.newInstance().newSAXParser()
              val res: Option[DefaultHandler] = parseXML(saxParser, handle, xmlInputStream, entryName)
              if (res.isDefined) {
                val mrRecords = res.get.asInstanceOf[MROSax].getMRO
                val eNBId: String = mrRecords.geteNB()
                log.info(s"[$cnt]$entryName => EnodeBID: $eNBId")
              } else {
                log.debug(s"Parsed $entryName and Returned None.")
              }
          }
      }
    }
  }

  protected def parseXML(parser: SAXParser, handle: DefaultHandler, ins: InputStream, entryName: String): Option[DefaultHandler] = {
    Try {
      parser.parse(ins, handle)
      handle
    } match {
      case Success(result) =>
        Some(result)
      case Failure(e) =>
        log.error(s"Failed to parse $entryName: ", e)
        None
    }
  }

  def readGZFile(fileName: String): Unit = {
    try {
      using(new FileInputStream(fileName)) {
        ins =>
          using(new GZIPInputStream(ins)) {
            gis =>
              using(new BufferedReader(new InputStreamReader(gis, "GBK"))) {
                br =>
                  while (br.ready()) {
                    val oneLine = br.readLine().replaceAll("null", "")
                    oneLine.length
                    //log.info(s"$oneLine")
                  }
              }
          }
      }
    } catch {
      case NonFatal(e) =>
        log.error(s"Failed to read $fileName: ", e)
    }
  }

  def readZipFile(fileName: String): Unit = {
    try {
      using(new ZipFile(new File(fileName), Charset.forName("GBK"))) {
        zipFile =>
          using(new BufferedInputStream(new FileInputStream(fileName))) {
            bis =>
              using(new ZipInputStream(bis)) {
                zis =>
                  val entry = zis.getNextEntry
                  val size = entry.getSize
                  if (size > 0) {
                    using(new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                      br =>
                        var cnt: Long = 1
                        while (br.ready() && (cnt <= size)) {
                          val line = br.readLine()
                          line.length
                          //log.info(s"$cnt: $line")
                          cnt += 1
                        }
                    }
                  }
              }
          }
      }
    } catch {
      case NonFatal(e) =>
        log.error(s"Failed to read $fileName: ", e)
    }
  }
}
