package com.wallace.demo.app.utils

import java.io.{FileInputStream, FileOutputStream, _}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.NumberFormat
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

  case class FileMetadata(file: File, offset: Long)

  private val factory: SAXParserFactory = SAXParserFactory.newInstance()
  private val DEFAULT_FILE_SIZE_THRESHOLD: Int = 128 * 1024 * 1024
  private var cnt: Int = 0
  private val DEFAULT_LENGTH: Long = 100 * 1024 * 1024L

  def writeToFile(data: Array[String], destFile: String, mode: String = "rw"): Unit = {
    using(new RandomAccessFile(destFile, "rw")) {
      output =>
        val currentLen: Long = output.length()
        require(currentLen < DEFAULT_LENGTH, s"Current length is bigger than default length of $destFile")
        output.seek(currentLen)
        output.setLength(DEFAULT_LENGTH)
        val dataLen: Int = Math.min(data.length, (DEFAULT_LENGTH - currentLen).toInt)
        val contents = new Array[String](dataLen)
        data.copyToArray(contents, 0, dataLen)
        contents.foreach(output.writeBytes)
    }
  }

  def readFileByLine(bufSize: Int, fcin: FileChannel, rBuf: ByteBuffer, fcout: FileChannel, wBuf: ByteBuffer): Unit = {
    val buffer: Array[Byte] = new Array[Byte](bufSize)
    val temp: Array[Byte] = new Array[Byte](500)
    while (fcin.read(rBuf) != -1) {
      val rSize = rBuf.position()
      rBuf.rewind()
      rBuf.get(buffer)
      rBuf.clear()

      var startNum: Int = 0
      var len: Int = 0
      (0 until rSize).foreach {
        i =>
          if (buffer(i) == 10.toByte) {
            startNum = i
            (0 until 500).foreach {
              k =>
                if (temp(k) == 0.toByte) {
                  len = i + k
                  (0 to i).foreach {
                    j =>
                      temp.update(k + j, buffer(j))
                  }
                }
            }
          }
      }

      val tempStr1: String = new String(temp, 0, len + 1, "GBK")
      temp.map(_ => 0.toByte)
      var endNum: Int = 0
      var k: Int = 0
      (0 until rSize).reverse.foreach {
        i =>
          if (buffer(i) == 10.toByte) {
            endNum = i
            ((i + 1) to rSize).foreach {
              j =>
                k += 1
                temp.update(k, buffer(j))
                buffer.update(j, 0.toByte)
            }
          }
      }
      val tempStr2: String = new String(buffer, startNum + 1, endNum - startNum, "GBK")
      val tempStr: String = tempStr1 + tempStr2
      var fromIndex: Int = 0
      var endIndex: Int = 0
      while ((endIndex = tempStr.indexOf("\n", fromIndex)) != -1) {
        val line = tempStr.substring(fromIndex, endIndex) + "\n" //按行截取字符串
        println(line)
        //写入文件
        writeFileByLine(fcout, wBuf, line)

        fromIndex = endIndex + 1
      }
    }
  }

  @SuppressWarnings(Array("static-access"))
  def writeFileByLine(fcout: FileChannel, wBuffer: ByteBuffer, line: String): Unit = {
    using(fcout) {
      fc =>
        val buf = wBuffer.put(line.getBytes("UTF-8"))
        buf.flip()
        fc.write(buf, fcout.size)
    }
  }


  def main(args: Array[String]): Unit = {
    //    // TODO READ GZ FILE
    //    val costTime1 = runtimeDuration(readGZFile("./demo/ScalaDemo/src/main/resources/AH_RM_20170926_all_all-cm_lte_cel-20170926000000-20170927000000-v2.0-20170926112500-001.csv.gz"))
    //    log.info(s"CostTime1: $costTime1 ms.")
    //
    //    // TODO READ ZIP FILE
    //    val costTime2 = runtimeDuration(readZipFile("./demo/ScalaDemo/src/main/resources/CDT_ZTE_V3.5_963847_20171201180000.zip"))
    //    log.info(s"CostTime2: $costTime2 ms.")

    //    // TODO READ TAR.GZ FILE
    //    val costTime3 = runtimeDuration {
    //      readTarGZFile("./demo/ScalaDemo/src/main/resources/HW_HN_OMC1-mr-134.175.57.16-20170921043000-20170921044500-20170921051502-001.tar.gz")
    //    }
    //    log.info(s"CostTime3: $costTime3 ms.")
    //
    //    // TODO Run test for filenamePrefixFromOffset
    //    val offset = filenamePrefixFromOffset(100L)
    //    log.info(s"Offset: $offset")

    // TODO readFileByByteBuffer
    val costTime4 = runtimeDuration {
      readFileByByteBuffer(new File("./demo/ScalaDemo/src/main/resources/testingData.csv"), "./demo/ScalaDemo/src/main/resources/")
    }
    log.info(s"CostTime4: $costTime4 ms.")
  }

  def readFileByByteBuffer(srcFile: File, destPath: String): Unit = {
    val outPutDestPath: String = appendOrRollFile(destPath)
    using(new FileInputStream(srcFile)) {
      in =>
        usingWithErrMsg(new FileOutputStream(outPutDestPath, true), s"Failed to write $outPutDestPath") {
          out =>
            val fcIn: FileChannel = in.getChannel
            val fcOut: FileChannel = out.getChannel
            writeToFileByByte(in, fcIn, fcOut)
        }
    }
  }

  def appendOrRollFile(path: String): String = {
    var offset: Long = 0L
    val destFilePath: String = {
      val tempPath = path.trim.replaceAll("""\\""", "/")
      if (tempPath.endsWith("/")) tempPath else tempPath + "/"
    }
    val fileList: Array[File] = new File(destFilePath).listFiles().filter(_.getName.startsWith("part-"))
    val prefixDestFile = destFilePath + "part-"
    val destFile: File = fileList.length match {
      case 0 => new File(prefixDestFile + filenamePrefixFromOffset(offset) + ".csv")
      case _ =>
        val tempFileAndOffset: FileMetadata = fileList.map {
          x =>
            val temp = x.getName.drop(5).dropRight(4).toLong
            FileMetadata(x, temp)
        }.maxBy(_.offset)
        offset = tempFileAndOffset.file.length() + tempFileAndOffset.offset
        tempFileAndOffset.file
    }
    log.warn(s"Offset: $offset.")
    if (destFile.length() <= DEFAULT_FILE_SIZE_THRESHOLD) {
      destFile.getPath
    } else {
      prefixDestFile + filenamePrefixFromOffset(offset) + ".csv"
    }
  }

  def filenamePrefixFromOffset(offset: Long): String = {
    val nf = NumberFormat.getInstance()
    nf.setMinimumIntegerDigits(20)
    nf.setMaximumFractionDigits(0)
    nf.setGroupingUsed(false)
    nf.format(offset)
  }

  def writeToFileByByte(fIns: FileInputStream, fcIn: FileChannel, fcOut: FileChannel, defaultBufCapacity: Int = 81920): Unit = {
    val srcFileSize: Long = fcIn.size()
    assert(srcFileSize < Int.MaxValue, s"The size of FileInputStream is too long: $srcFileSize > ${Int.MaxValue}.")
    val minBufferCapacity: Int = Math.min(srcFileSize.toInt, defaultBufCapacity)
    log.info(s"Before => fcIn: $srcFileSize, fcOut: ${fcOut.size()}, minBufferCapacity: $minBufferCapacity.")
    val buffer: ByteBuffer = ByteBuffer.allocate(minBufferCapacity)
    while (fIns.available() > 0) {
      buffer.clear()
      val r = fcIn.read(buffer)
      if (r > 0) {
        buffer.flip()
        fcOut.write(buffer)
      } else {
        log.warn("Buffer is empty.")
      }
    }

    log.info(s"After => fcIn: $srcFileSize, fcOut: ${fcOut.size()}")
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
}
