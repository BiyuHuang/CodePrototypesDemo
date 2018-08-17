package com.wallace.demo.app.utils

import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser

import scala.collection.JavaConverters._

/**
  * Created by 10192057 on 2018/7/30 0030.
  */
object JsonFormatter {
  def format(jsonStr: String): Map[String, String] = {
    //import scala.util.parsing.json.JSON
    val jParser: JSONParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE)
    val jObj: JSONObject = jParser.parse(jsonStr).asInstanceOf[JSONObject]
    convertJsonToMap(jObj)
  }

  private def convertJsonToMap(jsonObj: JSONObject, rootKey: String = ""): Map[String, String] = {
    jsonObj.keySet().asScala.flatMap {
      key =>
        val tempVal: AnyRef = jsonObj.get(key)
        val fieldKey: String = if (rootKey.nonEmpty) rootKey + "." + key else key
        tempVal match {
          case nObject: JSONObject =>
            if (nObject.isEmpty) Map(fieldKey -> "") else convertJsonToMap(nObject, fieldKey)
          case _ =>
            val value: String = if (tempVal == null) "" else tempVal.toString
            Map(fieldKey -> value)
        }
    }.toMap
  }
}
