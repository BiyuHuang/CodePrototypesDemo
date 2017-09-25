package com.wallace.demo.app.utils

import java.io._
import java.util.zip.GZIPInputStream

import com.wallace.demo.app.common.LogSupport


object FileUtils extends LogSupport {

  def main(args: Array[String]): Unit = {
    // TODO READ GZ FILE
    val file: File = new File("./demo/ScalaDemo/src/main/resources/text.csv.gz")
    val ins: FileInputStream = new FileInputStream(file)
    val gs: GZIPInputStream = new GZIPInputStream(ins)
    val br = new BufferedReader(new InputStreamReader(gs))
    //val sc: Scanner = new Scanner(gs)

    while (br.ready()) {
      val oneLine = br.readLine()
      log.info(s"$oneLine")
    }
  }
}
