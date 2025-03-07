package com.wallace.demo.app.utils

import java.io.{FileInputStream, FileOutputStream, _}
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.WatchService
import java.nio.{ByteBuffer, MappedByteBuffer}
import java.text.NumberFormat
import java.util.zip.{GZIPInputStream, ZipFile, ZipInputStream}
import javax.xml.parsers.{SAXParser, SAXParserFactory}

import com.typesafe.config.{Config, ConfigFactory}
import com.wallace.demo.app.common._
import com.wallace.demo.app.parsexml.{MROSax, SaxHandler}
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveInputStream}
import org.apache.commons.compress.archivers.zip
import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveInputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.example.data.Group
import org.apache.parquet.example.data.simple.SimpleGroupFactory
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.example.GroupWriteSupport
import org.apache.parquet.schema.{MessageType, MessageTypeParser}
import org.apache.spark.rdd.RDD

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Random, Success, Try}
import scala.xml.{Node, NodeSeq, XML}

object FileUtils extends Using {

  case class FileMetadata(file: File, offset: Long)

  def rwMappedByteBuffer(fileName: String, mode: String = "rw"): Unit = {
    using(new RandomAccessFile(fileName, mode)) {
      randomFile =>
        assert(randomFile.length() <= Int.MaxValue, s"The length of $fileName is bigger than 2GB.")
        val mappedBuffer: MappedByteBuffer = randomFile.getChannel.map(FileChannel.MapMode.READ_WRITE, 0, randomFile.length()).load()
        val dest = new Array[Byte](randomFile.length().toInt)
        mappedBuffer.get(dest)
        using(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(dest)))) {
          br =>
            while (br.ready()) {
              val line: String = br.readLine()
              logger.info(line)
            }
        }
    }
  }

  object FileSuffix {
    /** a csv file */
    val csvFileSuffix = ".csv"

    /** a temp file */
    val tempFileSuffix = ".temp"
  }

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

  def betterFilesFunc(): Unit = {
    import better.files.{File, FileMonitor}
    val f = File("./demo/ScalaDemo/src/main/resources/testingData.csv")
    logger.info(s"File Path: ${f.path}")
    //log.info(s"File CheckSum: ${f.sha512}")
    logger.info(s"File Line Size: ${f.lines(Charset.forName("UTF-8")).size}")
    logger.info(s"File Context Size: ${f.size / 1024L / 1024L} MB")
    logger.info(s"File LastModifiedTime: ${f.lastModifiedTime}")

    //TODO 普通的Java文件监控
    val watchDir: File = f.parent
    logger.info(s"File Parent: $watchDir, IsDirectory: ${watchDir.isDirectory}")
    import java.nio.file.{StandardWatchEventKinds => EventType}
    val service: WatchService = watchDir.newWatchService
    watchDir.register(service, events = Seq(EventType.ENTRY_MODIFY))
    var symbolCnt: Int = 0
    val watcher = new FileMonitor(watchDir, recursive = true) {
      //      override def onEvent(eventType: WatchEvent.Kind[Path], file: File, count: Int): Unit = eventType match {
      //        case EventType.ENTRY_CREATE => log.info(s"$file got created")
      //        case EventType.ENTRY_MODIFY => log.info(s"$file got modified")
      //        case EventType.ENTRY_DELETE => log.info(s"$file got deleted")
      //      }
      override def onModify(file: File, count: Int): Unit = {
        symbolCnt += 1
        logger.info(s"${file.name} got modified @$count")
      }
    }
    watcher.start()(ExecutionContext.global)

    //    //    //TODO Akka风格的文件监控
    //    implicit val system: ActorSystem = ActorSystem("Directory Watcher System")
    //    import better.files._
    //    import FileWatcher._
    //    val akkaWatcher: ActorRef = watchDir.newWatcher(recursive = true)
    //
    //    // register partial function for an event
    //    akkaWatcher ! on(EventType.ENTRY_DELETE) {
    //      case file if file.isDirectory => log.info(s"$file got deleted")
    //    }
    //
    //    // watch for multiple events
    //    akkaWatcher ! when(events = EventType.ENTRY_CREATE, EventType.ENTRY_MODIFY) {
    //      case (EventType.ENTRY_CREATE, file, count) => log.info(s"$file got created")
    //      case (EventType.ENTRY_MODIFY, file, count) => log.info(s"$file got modified $count times")
    //    }

    while (symbolCnt < 10) {
      Thread.sleep(1000)
      logger.info(s"Watching ${watchDir.name} ($symbolCnt)...")
    }
  }

  def main(args: Array[String]): Unit = {
    val rdd3: RDD[(String, String)] = ???
    rdd3.foreach {
      e =>
        println("name:" + e + "\nend")
        val originalFile = e._1
        val innerFile = e._2
        var f1 = originalFile.substring(6, originalFile.length)
        val f_new = new File(f1)
        val zf: zip.ZipFile = new org.apache.commons.compress.archivers.zip.ZipFile(f_new, "gbk")
        val packinfo: ZipArchiveEntry = zf.getEntry(innerFile)
        val innerFileInputStream = zf.getInputStream(packinfo)
        if (innerFile.endsWith(".zip")) {
          val zais: ZipArchiveInputStream = new ZipArchiveInputStream(zf.getInputStream(packinfo), "UTF-8", true)
          var innerzae: ZipArchiveEntry = zais.getNextZipEntry
          while (innerzae != null) {
            val size = innerzae.getSize
            println(s"Entry Name: ${innerzae.getName}, Entry Size: $size.")
            val defaultSize: Long = Math.min(Runtime.getRuntime.freeMemory(), Int.MaxValue)
            println(s"FreeMemory: ${Runtime.getRuntime.freeMemory() / (1024 * 1024)} MB. Default Bytes Size: $defaultSize Bytes")
            val currentSize: Long = if (size < 0) defaultSize else size
            val bos = new ByteArrayOutputStream(currentSize.toInt)
            IOUtils.copy(zais, bos, 40960)
            val res: ByteArrayInputStream = new ByteArrayInputStream(bos.toByteArray)
            println(s"third:${innerzae.getName},$zais")
            processCSV(innerzae.getName, res)
            innerzae = zais.getNextZipEntry
          }
        } else {
          println(s"$originalFile")
          processCSV(originalFile, innerFileInputStream)
        }
        zf.close()
    }

    def processCSV(filename: String, ins: InputStream): Unit = {
      val br: BufferedReader = new BufferedReader(new InputStreamReader(ins))
      var line: Option[String] = Option(br.readLine())
      while (line.isDefined) {
        println("line:" + line.get)
        line = Option(br.readLine())
      }
    }
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
    logger.info(s"CostTime3: $costTime3 ms.")

    //    // TODO Run test for filenamePrefixFromOffset
    //    val offset = filenamePrefixFromOffset(100L)
    //    log.info(s"Offset: $offset")

    //    // TODO readFileByByteBuffer
    //    val costTime4 = runtimeDuration {
    //      readFileByByteBuffer(new File("./demo/ScalaDemo/src/main/resources/testingData.csv"), "./demo/ScalaDemo/src/main/resources/")
    //    }
    //    log.info(s"CostTime4: $costTime4 ms.")

    //TODO Try Catch Exception
    //    val data = Array(1, 2, 3, 4)
    //    var size = data.length
    //    while (size > 0) {
    //      size -= 1
    //      try {
    //        if (size == 2) throw new Exception("test exception")
    //        log.info(data(size).toString)
    //      } catch {
    //        case e: Exception =>
    //          log.error(e.getMessage)
    //      }
    //    }

    //TODO betterFilesFunc
    //    betterFilesFunc()

    // TODO recursiveDelDirsAndFiles
    //    val f: File = new File("./demo/ScalaDemo/src/main/resources/temp/")
    //    recursiveDelDirsAndFiles(f)
    //
    //    val a = Array("test", "test/test1", "test/test1/test2", "test33/")
    //    a.groupBy(x => x.length).filterNot(x => x._1 == 1)

    // TODO GetTotalLines
    val srcFileName = "./demo/ScalaDemo/src/main/resources/testingData.csv"
    val srcFile = new File(srcFileName)
    val startTime = System.currentTimeMillis()
    //    val totalLines = getTotalLines(testFile)
    val totalLines = getTotalLines(srcFile)
    val endTime = System.currentTimeMillis()
    logger.info(s"[$srcFileName]TotalLines: $totalLines, CostTime: ${endTime - startTime} ms.")

    // TODO Read readZipArchiveFile
    //val fileName = "./demo/ScalaDemo/src/main/resources/FDD-LTE_MRS_ERICSSON_OMC1_335110_20180403101500.zip"
    //    val fileName = "./demo/ScalaDemo/src/main/resources/FDD-LTE_MRS_ERICSSON_OMC1_335112_20180403101500.xml.zip"
    val fileName = "./demo/ScalaDemo/src/main/resources/FDD-LTE_MRS_ZTE_OMC1_637784_20170522204500.zip"
    val costTime4: Double = runtimeDuration(readZipArchiveFile(fileName))
    logger.info(s"CostTime4: $costTime4 ms.")

    //TODO Get File Header
    var res: Option[Array[(String, Array[Byte])]] = None
    val costTime5 = runtimeDuration {
      res = Some(getFileHeader("./demo/ScalaDemo/src/main/resources/"))
    }
    res.get.foreach {
      elem =>
        logger.info(s"FileName: ${elem._1}, File Header Bytes: ${elem._2.take(3).mkString("_")}")
    }
    logger.info(s"CostTime5: $costTime5 ms.")

  }

  private def getTotalLines(srcFile: File): Int = {
    val reader = new LineNumberReader(new FileReader(srcFile))
    reader.skip(srcFile.length())
    val totalLines = reader.getLineNumber
    reader.close()
    totalLines
    //    using(new LineNumberReader(new FileReader(srcFile))) {
    //      reader =>
    //        //        var totalLines = 0
    //        //        var strLine = reader.readLine
    //        //        while (strLine != null) {
    //        //          totalLines += 1
    //        //          strLine = reader.readLine
    //        //        }
    //        //
    //        //        totalLines
    //
    //        reader.skip(srcFile.length())
    //        reader.getLineNumber
    //    }
  }

  private def getTotalLines(fileName: String): Int = {
    val srcFile = new File(fileName)
    getTotalLines(srcFile)
  }

  def readFileByByteBuffer(srcFile: File, destPath: String): Unit = {
    usingWithErrMsg(new FileInputStream(srcFile), s"Failed to read ${srcFile.getName}.") {
      in =>
        val outPutDestPath = appendOrRollFile(destPath)
        using(new FileOutputStream(outPutDestPath, true)) {
          out =>
            val fcIn = in.getChannel
            val fcOut = out.getChannel
            writeToFileByByte(in, fcIn, fcOut)
        }
    }
  }

  def appendOrRollFile(path: String): File = {
    var offset: Long = null.asInstanceOf[Long]
    val destFilePath: String = {
      val tempPath = path.trim.replaceAll("""\\""", "/")
      if (tempPath.endsWith("/")) tempPath else tempPath + "/"
    }
    val fileList: Array[File] = new File(destFilePath).listFiles().filter(x => x.getName.startsWith("part-") && x.isFile)
    val prefixDestFile = destFilePath + s"part-${Thread.currentThread().getId}-"

    val destFile: File = fileList.length match {
      case 0 => new File(prefixDestFile + filenamePrefixFromOffset(offset) + FileSuffix.csvFileSuffix)
      case _ =>
        val tempFileAndOffset: FileMetadata = fileList.map {
          elem =>
            val offset = elem.getName.reverse.substring(4, 24).reverse.toLong
            FileMetadata(elem, offset)
        }.maxBy(_.offset)
        offset = tempFileAndOffset.file.length() + tempFileAndOffset.offset
        tempFileAndOffset.file
    }
    logger.warn(s"Offset: $offset.")
    if (destFile.length() <= DEFAULT_FILE_SIZE_THRESHOLD) {
      destFile
    } else {
      new File(prefixDestFile + filenamePrefixFromOffset(offset) + FileSuffix.csvFileSuffix)
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
    logger.info(s"Before => fcIn: $srcFileSize, fcOut: ${fcOut.size()}, minBufferCapacity: $minBufferCapacity.")
    val buffer: ByteBuffer = ByteBuffer.allocate(minBufferCapacity)
    while (fIns.available() > 0) {
      buffer.clear()
      val r = fcIn.read(buffer)
      if (r > 0) {
        buffer.flip()
        fcOut.write(buffer)
      } else {
        logger.warn("Buffer is empty.")
      }
    }

    logger.info(s"After => fcIn: $srcFileSize, fcOut: ${fcOut.size()}")
  }

  def readGZFile(fileName: String): Unit = {
    try {
      using(new FileInputStream(fileName)) {
        ins =>
          using(new GZIPInputStream(ins)) {
            gis =>
              using(new BufferedReader(new InputStreamReader(gis, "GBK"))) {
                br =>
                  br.lines().toArray.foreach(line => logger.info(s"$line"))
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
        logger.error(s"Failed to read $fileName: ", e)
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
                          logger.info(s"${line.length}")
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
        logger.error(s"Failed to read $fileName: ", e)
    }
  }

  private def readZipArchiveFile(fileName: String): Unit = {
    import java.util.zip.ZipFile
    val f = new ZipFile(fileName)
    f.close()
    usingWithErrMsg(new FileInputStream(fileName), s"Failed to get input stream for $fileName") {
      inputStream =>
        using(new ZipArchiveInputStream(inputStream, "UTF-8")) {
          zipIns =>
            var entry: ZipArchiveEntry = zipIns.getNextZipEntry
            while (zipIns.canReadEntryData(entry) && entry != null) {

              if (entry.getName.endsWith(".zip")) {

              } else {
                val size = entry.getSize
                logger.info(s"Entry Name: ${entry.getName}, Entry Size: $size.")
                val defaultSize: Long = Math.min(Runtime.getRuntime.freeMemory(), Int.MaxValue)
                logger.debug(s"FreeMemory: ${Runtime.getRuntime.freeMemory() / (1024 * 1024)} MB. Default Bytes Size: $defaultSize Bytes")
                val currentSize: Long = if (size < 0) defaultSize else size
                val bos = new ByteArrayOutputStream(currentSize.toInt)
                IOUtils.copy(zipIns, bos, 40960)
                val res: ByteArrayInputStream = new ByteArrayInputStream(bos.toByteArray)
                using(new BufferedReader(new InputStreamReader(res))) {
                  br =>
                    while (br.ready()) {
                      logger.info(br.readLine())
                    }
                }
                bos.flush()
                bos.close()
              }
              entry = zipIns.getNextZipEntry
            }
        }
    }
  }

  private def readTarGZFile(fileName: String): Unit = {
    cnt = 0
    usingWithErrMsg(new FileInputStream(fileName), s"Failed to get input stream for $fileName") {
      fin =>
        val buf: Array[Byte] = new Array[Byte](8)
        fin.read(buf, 0, 8)
        val a = BigInt.apply(buf)
        println(a)
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

  def getFileHeader(rootPath: String): Array[(String, Array[Byte])] = {
    val file = new File(rootPath)
    file.listFiles().filter(_.isFile).map {
      f =>
        using(new FileInputStream(f)) {
          fin =>
            val buf: Array[Byte] = new Array[Byte](10)
            fin.read(buf, 0, 10)
            f.getName -> buf
        }
    }
  }

  def processSingleEntry(tarInput: TarArchiveInputStream, fileName: String): Unit = {
    val entry: TarArchiveEntry = tarInput.getCurrentEntry
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
              val res: Option[SaxHandler] = parseXML(saxParser, handle, xmlInputStream, entryName)
              if (res.isDefined) {
                val mrRecords = res.get.getResult
                val eNBId: String = mrRecords.geteNB()
                logger.info(s"[$cnt]$entryName => EnodeBID: $eNBId")
              } else {
                logger.debug(s"Parsed $entryName and Returned None.")
              }
          }
      }
    }
  }

  protected def parseXML(parser: SAXParser, handle: SaxHandler, ins: InputStream, entryName: String): Option[SaxHandler] = {
    Try {
      parser.parse(ins, handle)
      handle
    } match {
      case Success(result) =>
        Some(result)
      case Failure(e) =>
        logger.error(s"Failed to parse $entryName: ", e)
        None
    }
  }


  // TODO Recursive List Files
  def recursiveListFiles(rootFile: File): Array[File] = {
    if (rootFile.isFile) {
      Array(rootFile)
    } else {
      rootFile.listFiles().flatMap(recursiveListFiles)
    }
  }

  private def load(fileName: String): Config = {
    val projectConfigFile = fileName
    val udfConfigFile: Array[File] = Array(new File(SystemEnvUtils.getUserDir + "../conf/" + fileName))
    if (udfConfigFile.nonEmpty) {
      logger.debug(s"loading file[${udfConfigFile.head.getPath}] and resource[$projectConfigFile]")
      ConfigFactory.parseFile(udfConfigFile.head).withFallback(ConfigFactory.load(projectConfigFile))
    } else {
      logger.debug(s"loading resource[$projectConfigFile]")
      ConfigFactory.load(projectConfigFile)
    }
  }

  private def getAttrValue(node: Node, key: String, defaultValue: String = ""): String = node.attribute(key) match {
    case Some(res) => res.mkString.trim.toLowerCase
    case None => defaultValue
  }

  private def getNodeSeqText(nodeSeq: NodeSeq, defaultValue: String = ""): String = nodeSeq.text match {
    case v: String => v.trim.toLowerCase
    case _ => defaultValue
  }

  def parquetWriter(destPath: String, srcPath: String): Unit = {
    val schema: MessageType = MessageTypeParser.parseMessageType(
      """
        |message Pair {
        |OPTIONAL int32 id;
        |OPTIONAL binary city (UTF8);
        |OPTIONAL binary ip (UTF8);
        |OPTIONAL group time(LIST){
        |	 repeated group bag {
        |     optional binary elem (UTF8);
        |   }
        | }
        |}
      """.stripMargin)
    val conf: Configuration = HdfsManager.hdfsConf

    val factory: SimpleGroupFactory = new SimpleGroupFactory(schema)
    val destP: Path = new Path(destPath)
    val writeSupport: GroupWriteSupport = new GroupWriteSupport()
    GroupWriteSupport.setSchema(schema, conf)
    //val recordWriter = new ParquetOutputFormat[Group]().getRecordWriter(conf, destP, CompressionCodecName.UNCOMPRESSED)
    val values: Array[String] = "ShenZhen,192.168.0.1,2018-5-10 10:52:15".split(",")
    using(new ParquetWriter[Group](destP, conf, writeSupport)) {
      writer =>
        val group = factory.newGroup()
        if (values.length == 2) {
          val r = new Random
          values.indices.foreach {
            i =>
              group.append(schema.getFieldName(i), values(i))
          }

          val tmpG = group.addGroup("time")
          tmpG.append("ttl", r.nextInt(9) + 1)
          tmpG.append("ttl2", r.nextInt(9) + "_a")
          logger.info("Group String: " + tmpG.toString)
          writer.write(group)
        }
    }
  }

  def readXMLConfigFile(algPath: String): Map[String, AlgMetaData] = {
    val pluginParentFile: File = new File(algPath)
    val adapters: Array[File] = if (pluginParentFile.exists()) {
      recursiveListFiles(pluginParentFile).filter(_.getName.endsWith(".xml"))
    } else {
      Array.empty
    }
    if (adapters.isEmpty) {
      Map.empty
    } else {
      val res: Array[immutable.Seq[(String, Config, String, String,
        immutable.Seq[(String, String, String, String, Map[String, MethodMetaData])])]] = adapters.map {
        f =>
          val algXml = XML.loadFile(f)
          (algXml \ "algorithm").map {
            rootNode =>
              val pluginConfFileName: String = getAttrValue(rootNode, "confPath")
              val algorithmArgs: Config = if (pluginConfFileName.nonEmpty) load(pluginConfFileName) else ConfigFactory.empty()
              val dataType: String = getAttrValue(rootNode, "dataType")
              val fieldsSep: String = getAttrValue(rootNode, "fieldSeparator")
              val algorithmID = getAttrValue(rootNode, "id")
              assert(algorithmID.nonEmpty, "algorithmID must be nonEmpty.")
              val algorithmInfo: immutable.Seq[(String, String, String, String,
                Map[String, MethodMetaData])] = (rootNode \ "algorithminfo").map {
                treeNode =>
                  val className: String = getNodeSeqText(treeNode \ "className")
                  val target: String = getAttrValue(treeNode, "target")
                  val regionID: String = getAttrValue(treeNode, "regionid")
                  val algorithmKey: String = if (regionID.nonEmpty) target + "_" + regionID else target
                  val inputFields: String = getNodeSeqText(treeNode \ "inputFields")
                  val outputFields: String = getNodeSeqText(treeNode \ "outputFields")
                  val parsersInfo: Map[String, MethodMetaData] = (treeNode \ "fieldsProcess" \ "process").flatMap {
                    procNode =>
                      val method: String = getAttrValue(procNode, "method")
                      val pInputFields: String = getNodeSeqText(procNode \ "inputFields")
                      val pOutputFields: String = getNodeSeqText(procNode \ "outputFields")
                      val conf: Map[String, String] = (procNode \ "conf" \ "item").flatMap {
                        iNode =>
                          val key = getAttrValue(iNode, "name")
                          val value = getNodeSeqText(iNode)
                          Map(key -> value)
                      }.toMap
                      Map(method -> MethodMetaData(pInputFields, pOutputFields, conf))
                  }.toMap
                  (algorithmKey, className, inputFields, outputFields, parsersInfo)
              }
              (algorithmID, algorithmArgs, dataType, fieldsSep, algorithmInfo)
          }
      }
      res.flatMap {
        algID =>
          algID.flatMap {
            adaptor =>
              adaptor._5.map {
                algInfo =>
                  (algInfo._1, AlgMetaData(adaptor._2, algInfo._2, ParserMetaData(adaptor._3, adaptor._4, algInfo._3, algInfo._4, algInfo._5)))
              }
          }
      }.toMap
    }
  }

  // TODO Delete file
  def deleteFile(file: File): Boolean = {
    var delState = false
    if (file.exists()) {
      if (file.canExecute) {
        if (file.delete()) {
          delState = true
        } else {
          logger.error(s"Failed to delete ${
            file.getCanonicalPath
          }.")
          delState = false
        }
      } else {
        if (file.setExecutable(true)) {
          if (file.delete()) {
            delState = true
          } else {
            logger.error(s"Failed to delete ${
              file.getCanonicalPath
            }.")
            delState = false
          }
        } else {
          logger.warn(s"Failed to set executable for ${
            file.getName
          }")
          file.deleteOnExit()
        }
      }
    } else {
      logger.warn(s"${
        file.getName
      } doesn't exist or has no execute permission.")
    }
    delState
  }

  // TODO Recursive Delete Dirs and Files
  def recursiveDelDirsAndFiles(rootFile: File): Boolean = {
    var delState: Boolean = false
    if (rootFile.isDirectory) {
      val files: Array[File] = rootFile.listFiles
      if (files.nonEmpty) {
        files.foreach {
          file =>
            if (file.isDirectory) {
              recursiveDelDirsAndFiles(file)
            } else {
              delState = deleteFile(file)
            }
        }
      } else {
        logger.debug(s"${
          rootFile.getName
        } is an empty directory, just delete it.")
      }
      delState = deleteFile(rootFile)
    } else {
      delState = deleteFile(rootFile)
    }

    delState
  }
}

