package com.wallace.demo.app.topKDemo

import java.io._

import com.wallace.demo.app.common.LogSupport

import scala.collection.{immutable, mutable}
import scala.util.{Failure, Success, Try}

/**
  * Created by 10192057 on 2018/8/8 0008.
  */
object BigDataTopNDemo extends LogSupport {
  private val splitNum = 10240

  //  private val fileWriterPool: mutable.HashMap[Int, FileWriter] = new mutable.HashMap[Int, FileWriter]()

  def main(args: Array[String]): Unit = {
    //TODO BigData( A: key.hashCode % 10240) => SmallData( a1, a2)
    //TODO (0 until 10240).foreach( Traversal <a1, a2>, Return intersection<a1, a2>)

    assert(args.length >= 2, "Usage: [FileName]")

    val data1: BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(args(0))))
    val data2: BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(args(1))))
    val splitFiles1 = hashSplitFile(data1, args(0))
    val splitFiles2 = hashSplitFile(data2, args(1))


    val intersection: immutable.Seq[File] = (0 until splitNum).map {
      index =>
        if (splitFiles1.contains(index) && splitFiles2.contains(index)) {
          Array(splitFiles1(index), splitFiles2(index))
        } else {
          Array.empty[File]
        }
    }.filter(_.nonEmpty).flatten
  }

  def hashSplitFile(data: BufferedReader, fileName: String, append: Boolean = true): mutable.HashMap[Int, File] = {
    val fileWriterPool: mutable.HashMap[Int, FileWriter] = new mutable.HashMap[Int, FileWriter]()
    val splitFiles: mutable.HashMap[Int, File] = new mutable.HashMap[Int, File]()
    while (data.ready()) {
      Try {
        val record: String = data.readLine()
        val indexKey: Int = record.hashCode % splitNum
        val splitFile: File = new File(s"$fileName.$indexKey")
        val fw: FileWriter = if (fileWriterPool.contains(indexKey)) {
          fileWriterPool(indexKey)
        } else {
          val fw = new FileWriter(splitFile, append)
          fileWriterPool.put(indexKey, fw)
          fw
        }

        if (!splitFiles.contains(indexKey)) splitFiles.put(indexKey, splitFile)
        fw.write(record)
        fw.flush()
      } match {
        case Success(_) => logger.debug(s"Succeed to write record for $fileName.")
        case Failure(e) => logger.error(s"Aborted to split $fileName", e)
      }
    }
    splitFiles
  }
}
