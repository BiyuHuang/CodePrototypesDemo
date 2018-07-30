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
    getAllFields(jObj)
  }

  def getAllFields(jsonObj: JSONObject, rootKey: String = ""): Map[String, String] = {
    jsonObj.keySet().asScala.flatMap {
      key =>
        val tempVal: AnyRef = jsonObj.get(key)
        tempVal match {
          case nObject: JSONObject =>
            getAllFields(nObject, key)
          case _ =>
            val fieldKey: String = if (rootKey.nonEmpty) rootKey + "." + key else key
            Map(fieldKey -> tempVal.toString)
        }
    }.toMap
  }
}
