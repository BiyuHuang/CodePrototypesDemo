package com.wallace.demo.app.common

import java.io.{BufferedInputStream, ByteArrayInputStream, FileOutputStream, IOException}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import org.apache.hadoop.io.IOUtils
import org.apache.hadoop.io.compress.{CompressionCodecFactory, CompressionOutputStream}

/**
 * com.wallace.demo.app.common
 * Created by Wallace on 2017/12/5 0005.
 */
trait HdfsSupportHA extends ProjectConfig {
  def hdfsConf: Configuration

  private var errorHdfsCnt: Int = 0
  private var usingHdfsCnt: Int = 0

  def usingHdfs(errMsg: String)(f: FileSystem => Unit): Unit = {
    this.synchronized(usingHdfsCnt += 1)
    if (usingHdfsCnt >= 10000) {
      logger.info("Current error count :" + errorHdfsCnt)
      this.synchronized(usingHdfsCnt = 0)
    }
    try {
      val hdfs: FileSystem = FileSystem.get(hdfsConf)
      f(hdfs)
    } catch {
      case e: IOException =>
        this.synchronized(errorHdfsCnt += 1)
        logger.error(errMsg + s" error Count:$errorHdfsCnt.", e)
        throw e
      case ex: Throwable =>
        this.synchronized(errorHdfsCnt += 1)
        logger.error(errMsg + s" error Count:$errorHdfsCnt ", ex)
        throw ex
    }
  }

  def upload(source: String, target: String, overwrite: Boolean): Unit = {
    usingHdfs("upload failed.") {
      hdfs =>
        val in = new BufferedInputStream(this.getClass.getResourceAsStream(source))
        val out = hdfs.create(new Path(target), overwrite)
        try {
          IOUtils.copyBytes(in, out, 4096, true)
        } finally {
          if (in != null) in.close()
          if (out != null) out.close()
        }
    }
  }

  def delete(target: String): Unit = {
    usingHdfs("delete failed.") {
      hdfs =>
        val path = new Path(target)
        if (hdfs.exists(path)) {
          logger.info(s"Start to delect: ${path.toString}")
          hdfs.delete(path, true)
          logger.info(s"End to delect: ${path.toString}")
        } else {
          logger.warn(s"Delete Path Failed! Path: ${path.toString} is not exists!")
        }
    }
  }

  def emptyDir(dir: String): Unit = {
    usingHdfs("emptyDir failed.") {
      hdfs =>
        val p = new Path(dir)
        if (hdfs.getFileStatus(p).isDirectory) {
          val files = hdfs.listFiles(p, false)
          while (files.hasNext) {
            hdfs.delete(files.next().getPath, true)
          }
        }
    }
  }

  def download(source: String, target: String): Unit = {
    usingHdfs("download failed.") {
      hdfs =>
        val srcPath = source
        val out = new FileOutputStream(s"$target")
        val files = hdfs.listFiles(new Path(srcPath), false)
        try {
          while (files.hasNext) {
            val file = files.next()
            if (file.getPath.toString.contains("part-")) {
              val in = hdfs.open(new Path(file.getPath.toString))
              try {
                IOUtils.copyBytes(in, out, 4096, false)
              }
              finally {
                in.close()
              }
            }
          }
        } finally {
          if (out != null) out.close()
        }
    }
  }

  def downloadEx(source: String, target: String): Unit = {
    usingHdfs("download failed.") {
      hdfs =>
        val srcPath = source
        val out = new FileOutputStream(s"$target")
        val files = hdfs.listFiles(new Path(srcPath), false)
        try {
          while (files.hasNext) {
            val file = files.next()
            val in = hdfs.open(new Path(file.getPath.toString))
            try {
              IOUtils.copyBytes(in, out, 4096, false)
            }
            finally {
              if (in != null) in.close()
            }
          }
        } finally {
          if (out != null) out.close()
        }
    }
  }

  def write(source: String, target: String): Unit = {
    usingHdfs("write failed.") {
      hdfs =>
        val in = new ByteArrayInputStream(source.getBytes("UTF-8"))
        val out = hdfs.create(new Path(target))
        try {
          IOUtils.copyBytes(in, out, source.length, true)
        } finally {
          if (in != null) in.close()
          if (out != null) out.close()
        }
    }
  }

  def writeCompressionFile(source: String, target: String): Unit = {
    usingHdfs("write compress file to hdfs failed.") {
      hdfs =>
        val compressionCodecClassName: String = "org.apache.hadoop.io.compress.GzipCodec"
        val factory = new CompressionCodecFactory(hdfsConf)
        val codec = factory.getCodecByClassName(compressionCodecClassName)
        val extension = codec.getDefaultExtension

        val in: ByteArrayInputStream = new ByteArrayInputStream(source.getBytes("UTF-8"))
        val outputStream: FSDataOutputStream = hdfs.create(new Path(target + extension))
        val compressionOutStream: CompressionOutputStream = codec.createOutputStream(outputStream)
        try {
          IOUtils.copyBytes(in, compressionOutStream, hdfsConf, true)
          //IOUtils.copyBytes(in, compressionOutStream, source.length, true)
        } finally {
          in match {
            case _: ByteArrayInputStream => in.close()
            case _ =>
          }
          compressionOutStream match {
            case _: CompressionOutputStream => IOUtils.closeStream(compressionOutStream)
            case _ =>
          }
          outputStream match {
            case _: FSDataOutputStream => IOUtils.closeStream(outputStream)
            case _ =>
          }
        }
    }
  }

  def append(source: String, target: String): Unit = {
    usingHdfs("append failed") {
      hdfs =>
        val in = new ByteArrayInputStream(source.getBytes("UTF-8"))
        val out = if (hdfs.exists(new Path(target))) {
          hdfs.append(new Path(target))
        }
        else {
          hdfs.create(new Path(target))
        }
        try {
          IOUtils.copyBytes(in, out, source.length, true)
        } finally {
          if (in != null) in.close()
          if (out != null) out.close()
        }
    }
  }


  def listFiles(path: String): List[String] = {
    var result: List[String] = Nil
    usingHdfs("listFiles failed.") {
      hdfs =>
        val hdfsPath = new Path(path)
        if (hdfs.exists(hdfsPath)) {
          result = hdfs.listStatus(hdfsPath).map(_.getPath.getName).toList
          result.foreach(x => logger.debug(x))
        }
    }
    result
  }
}
