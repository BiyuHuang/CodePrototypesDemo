/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.common

import java.io.{File, FileOutputStream}
import java.util.Date
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, Path}
import org.apache.hadoop.io.IOUtils

import scala.util.{Failure, Success, Try}

/**
 * Created by wallace on 2017/12/4.
 */
object HdfsManager extends HdfsSupportHA with Using {
  lazy val currentPath: String = System.getProperty("user.dir")

  val hdfsConf: Configuration = configHdfs()

  def configHdfs(): Configuration = {
    val hdfsConf: Configuration = new Configuration()
    val configLists: List[String] = List(s"${configHome}core-site.xml", s"${configHome}hdfs-site.xml", s"${configHome}hive-site.xml")
    configLists.foreach {
      file =>
        if (new java.io.File(file).exists()) {
          hdfsConf.addResource(new Path(file))
          logger.debug(s"HdfsFileManager addResource from config directory: $file.current directory $configHome")
        } else {
          val configPath = file.split("/").init.mkString("/")
          val configFilename = file.split("/").last

          if (new java.io.File(configFilename).exists()) {
            hdfsConf.addResource(new Path(configFilename))
            logger.debug(s"HdfsFileManager addResource from current directory $currentPath: adding $configFilename: Cannot find file in config directory $configPath")
          } else {
            logger.debug(s"HdfsFileManager addResource adding $configFilename failure: Cannot find file in config directory $configPath and current directory $currentPath")
          }
        }
    }
    hdfsConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.GzipCodec")
    hdfsConf
  }


  def fileSize(filename: String): Long = {
    var size: Long = 0L
    usingHdfs("get file status failure") {
      hdfs =>
        val status = hdfs.getFileStatus(new Path(filename))
        size = if (status.isDirectory) {
          listFiles(filename).map(file => fileSize(s"$filename/$file")).sum
        } else {
          hdfs.getFileStatus(new Path(filename)).getLen
        }
    }
    size
  }

  def isDirectory(dir: String): Boolean = {
    var res: Boolean = false
    usingHdfs("Check dir exists failed.") {
      hdfs =>
        val p = new Path(dir)
        res = hdfs.isDirectory(p)
    }
    res
  }

  def isFile(fileName: String): Boolean = {
    var res: Boolean = false
    usingHdfs("Check fileName exists failed.") {
      hdfs =>
        val p = new Path(fileName)
        res = hdfs.getFileStatus(p).isFile
    }
    res
  }

  // HdfsSupport 中的 upload 在 EmsPmDataImportSpec 用例中总会上传失败，原因未知，暂时先重写一个
  def upload(src: String, target: String): Unit = {
    usingHdfs("upload failed!") {
      hdfs =>
        hdfs.copyFromLocalFile(false, true, new Path(src), new Path(target))
    }
  }

  def rename(src: String, target: String): Unit = {
    usingHdfs("rename failed!") {
      hdfs =>
        val srcpath = new Path(src)
        val targetpath = new Path(target)

        if (!hdfs.exists(targetpath.getParent)) {
          hdfs.mkdirs(targetpath.getParent)
        } else {
          if (hdfs.exists(targetpath)) delete(target)
        }

        hdfs.rename(srcpath, targetpath)
    }
  }

  def mv(srcFiles: List[String], targetPath: String): Unit = {
    usingHdfs("mv files failed!") {
      hdfs =>
        srcFiles.map(fileName => hdfs.rename(new Path(fileName), new Path(s"$targetPath/${fileName.reverse.takeWhile(_ != '/').reverse}")))
    }
  }

  def get(srcPath: String, localPath: String, fileFilter: String => Boolean = { _ => true }): Unit = {
    usingHdfs("get failed!") {
      hdfs =>
        val files = hdfs.listFiles(new Path(srcPath), false)
        val local = new File(localPath)
        if (!local.exists()) local.mkdirs()
        while (files.hasNext) {
          val file = files.next()
          if (fileFilter(file.getPath.getName)) hdfs.copyToLocalFile(false, file.getPath, new Path(localPath + s"/${file.getPath.getName}"), true)
        }
    }
  }

  def hdfsDownLoadFile(source: String, target: String, append: Boolean = false): Unit = {
    usingHdfs("download failed.") {
      hdfs =>
        val srcPath = source
        val out: FileOutputStream = new FileOutputStream(s"$target", append)
        if (hdfs.exists(new Path(srcPath))) {
          val files = hdfs.listFiles(new Path(srcPath), false)
          logger.info("files path" + files)
          try {
            while (files.hasNext) {
              logger.info("while start")
              val file = files.next()
              if (file.getPath.toString.contains("part-")) {
                val in: FSDataInputStream = hdfs.open(new Path(file.getPath.toString))
                try {
                  IOUtils.copyBytes(in, out, 4096, false)
                } finally {
                  in match {
                    case _: FSDataInputStream => in.close()
                    case _ =>
                  }
                }
              }
            }
          } catch {
            case e: Throwable => e.printStackTrace()
          } finally {
            out match {
              case _: FileOutputStream => out.close()
              case _ =>
            }
          }
        } else {
          logger.debug("download Error:" + srcPath + " not exist ")
        }
    }
  }

  def appendDir(srcPath: String, dstPath: String): Unit = {
    logger.debug(s"try to append $srcPath to $dstPath ...")
    listFiles(srcPath).foreach(fileName => if (fileName.startsWith("part-")) {
      appendFile(s"$srcPath/$fileName", s"$dstPath/$fileName")
    })
  }

  def appendFile(src: String, target: String): Unit = {
    logger.debug(s"try to append $src to $target ...")
    usingHdfs("append failed") {
      hdfs =>
        val in: FSDataInputStream = hdfs.open(new Path(src))
        val out: FSDataOutputStream = if (hdfs.exists(new Path(target))) {
          hdfs.create(new Path(target + System.currentTimeMillis()))
          //          hdfs.append(new Path(target))
        }
        else {
          hdfs.create(new Path(target))
        }
        try {
          IOUtils.copyBytes(in, out, 4096, true)
        } finally {
          if (in != null) in.close()
          if (out != null) out.close()
        }
    }
    logger.debug(s"append $src to $target complete")
  }

  def getDfsNameServices: String = {
    var temp: Option[String] = None
    usingHdfs("") {
      hdfs =>
        temp = Some(hdfs.getCanonicalServiceName)
    }
    val dfsName = temp.getOrElse(hdfsConf.get("dfs.nameservices"))
    logger.info(s"getDfsNameServices return $dfsName")
    dfsName
  }

  def downloadFile(hdfsFileName: String, localFileName: String, delSourceFile: Boolean = false): Unit = {
    usingHdfs("download one file to local") {
      hdfs =>
        hdfs.copyToLocalFile(delSourceFile, new Path(hdfsFileName), new Path(localFileName))
    }
  }


  def downloadGzipFilesToLocal(remoteHdfsPath: String, localFileName: String): Unit = {
    logger.info(s"downloadGzipFilesToLocal: From $remoteHdfsPath to $localFileName,start time: " + new Date().toString)
    usingHdfs("download Hdfs Gzip Files error.") {
      hdfs =>
        val files = hdfs.listFiles(new Path(remoteHdfsPath), false)
        val gzFiles = Stream.continually(files.hasNext).takeWhile(_.equals(true))
          .map(_ => files.next().getPath).filter(x => x.getName.endsWith(".gz"))

        //create local path if not exist
        createLocalPath(localFileName)

        // combine all gz files into
        val buffer = new Array[Byte](8192)
        val gzStreams = gzFiles.map {
          x =>
            Try {
              new GZIPInputStream(hdfs.open(x))
            }
        }.filter(_.isSuccess).map(_.get)

        try {
          using(new FileOutputStream(localFileName)) {
            localFile =>
              using(new GZIPOutputStream(localFile)) {
                bos =>
                  gzStreams.foreach(x => Stream.continually(x.read(buffer)).takeWhile(_ != -1).foreach(bos.write(buffer, 0, _)))
                  bos.flush()
              }
          }
        } finally {
          gzStreams.foreach(x => Try {
            x.close()
          })
        }

    }
    logger.info(s"downloadGzipFilesToLocal: From $remoteHdfsPath to $localFileName,end time: " + new Date().toString)
  }

  private def createLocalPath(localFileName: String): Boolean = {
    Try {
      val localFilePath = localFileName.replace("\\", "/").split("/").toList.init
      localFilePath match {
        case Nil =>
          false
        case filePath =>
          val path = filePath.mkString("/")
          val file = new File(path)
          val result = file.mkdirs()
          val osName = System.getProperties.getProperty("os.name")
          if (osName.equalsIgnoreCase("Linux")) Runtime.getRuntime.exec(s"chmod -R 777  $path")
          result
      }
    } match {
      case Success(result) =>
        result
      case Failure(e) =>
        logger.error(s"Create directories or change directory authority for file $localFileName. Throw exceptions: ", e)
        false
    }
  }

  def downloadFile(tgtFileName: String, destFileName: String): Unit = {
    usingHdfs("") {
      hdfs =>
        // 调用open方法进行下载，参数HDFS路径
        val in = hdfs.open(new Path(tgtFileName))
        // 创建输出流，参数指定文件输出地址
        val out = new FileOutputStream(destFileName)
        // 使用Hadoop提供的IOUtils，将in的内容copy到out，设置buffSize大小，是否关闭流设置true
        IOUtils.copyBytes(in, out, 4096, true);
    }
  }
}
