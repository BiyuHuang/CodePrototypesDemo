package com.wallace.demo.app.xmlparser

import java.net.URL

import com.wallace.demo.app.common.LogSupport

import scala.collection.immutable
import scala.xml.{NodeSeq, XML}

/**
  * Created by 10192057 on 2017/9/21 0021.
  */
object XmlParser extends LogSupport {
  def makePath(fileName: String): String = {
    val url: URL = this.getClass.getProtectionDomain.getCodeSource.getLocation
    val sep: String = scala.sys.props.getOrElse("file.separator", "/")

    val filePath: String = java.net.URLDecoder.decode(url.getPath, "utf-8")
    if (filePath.endsWith(".jar")) {
      filePath.take(filePath.lastIndexOf(sep)) + sep + fileName
    } else {
      filePath + sep + fileName
    }
  }

  def main(args: Array[String]): Unit = {
    val srcFile: String = makePath("FixExample.xml")
    val xmlFile = XML.loadFile(srcFile)

    // TODO 获取所有子节点
    val allFields: NodeSeq = xmlFile \\ "_"
    log.info(s"${allFields.head.toString()}")

    // TODO 获取fields
    val headerField = xmlFile \ "header" \ "field"
    val field = xmlFile \\ "field"

    // TODO 获取特定的属性(attribute)
    //val fieldAttributes = (someXml \ "header" \ "field").map(_ \ "@name")
    val fieldAttributes = xmlFile \ "header" \ "field" \\ "@name"

    // TODO 查找并输出属性值和节点值的映射
    val mapRes: immutable.Seq[(NodeSeq, String, NodeSeq)] = (xmlFile \ "header" \ "field").map(n => (n \ "@name", n.text, n \ "@required"))

    // TODO 按特定条件获取节点
    val resultXml1 = (xmlFile \\ "message").filter(_.attribute("name").exists(x => x.text.equals("Logon")))
    val resultXml2 = (xmlFile \\ "message").filter(x => (x \ "@name").text == "Logon")
  }
}
