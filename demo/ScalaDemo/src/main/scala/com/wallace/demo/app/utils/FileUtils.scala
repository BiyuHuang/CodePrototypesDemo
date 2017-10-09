package com.wallace.demo.app.utils

import java.io._
import java.util.zip.GZIPInputStream

import com.wallace.demo.app.common.LogSupport


object FileUtils extends LogSupport {

  def main(args: Array[String]): Unit = {
    // TODO READ GZ FILE
    val file: File = new File("./demo/ScalaDemo/src/main/resources/AH_RM_20170926_all_all-cm_lte_cel-20170926000000-20170927000000-v2.0-20170926112500-001.csv.gz")
    val ins: FileInputStream = new FileInputStream(file)
    val gs: GZIPInputStream = new GZIPInputStream(ins)
    val br = new BufferedReader(new InputStreamReader(gs, "GBK"))
    //val sc: Scanner = new Scanner(gs, "GBK")

    //    while (sc.hasNextLine) {
    //      val oneLine = sc.nextLine().replaceAll("null", "")
    //      log.info(s"$oneLine")
    //    }

    while (br.ready()) {
      val oneLine = br.readLine().replaceAll("null", "")
      log.info(s"$oneLine")
    }
  }
}
