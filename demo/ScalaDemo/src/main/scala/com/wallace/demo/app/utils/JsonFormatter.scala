package com.wallace.demo.app.utils

import net.minidev.json.parser.JSONParser
import net.minidev.json.{JSONArray, JSONObject}

import scala.collection.JavaConverters._
import scala.collection.Map

/**
  * Created by 10192057 on 2018/7/30 0030.
  */
object JsonFormatter {
  def format(jsonStr: String): Map[String, String] = {
    //import scala.util.parsing.json.JSON
    convertJsonToMap(convertStrToJson(jsonStr))
  }

  def convertStrToJson(jsonStr: String,
                       permissifMode: Int = JSONParser.DEFAULT_PERMISSIVE_MODE): JSONObject = {
    val jParser: JSONParser = new JSONParser(permissifMode)
    jParser.parse(jsonStr).asInstanceOf[JSONObject]
  }

  def convertJsonToMap(jsonObj: JSONObject, rootKey: String = ""): Map[String, String] = {
    jsonObj.keySet().asScala.flatMap {
      key =>
        val tempVal: AnyRef = jsonObj.get(key)
        val fieldKey: String = if (rootKey.nonEmpty) rootKey + "." + key else key
        tempVal match {
          case nObject: JSONObject =>
            if (nObject.isEmpty) Map(fieldKey -> "") else convertJsonToMap(nObject, fieldKey)
          case nArray: JSONArray =>
            nArray.asScala.flatMap {
              elem =>
                elem match {
                  case eObject: JSONObject =>
                    convertJsonToMap(eObject, fieldKey)
                  case value: String =>
                    Map(fieldKey -> value)
                }
            }
          case _ =>
            val value: String = if (tempVal == null) "" else tempVal.toString
            Map(fieldKey -> value)
        }
    }.toMap
  }

  def convertJsonToArray(jsonObj: JSONObject, rootKey: String = ""): Array[(String, String)] = {
    jsonObj.keySet().asScala.flatMap {
      key =>
        val tempVal: AnyRef = jsonObj.get(key)
        val fieldKey: String = if (rootKey.nonEmpty) rootKey + "." + key else key
        tempVal match {
          case nObject: JSONObject =>
            if (nObject.isEmpty) Map(fieldKey -> "") else convertJsonToArray(nObject, fieldKey)
          case nArray: JSONArray =>
            nArray.asScala.flatMap {
              elem =>
                elem match {
                  case eObject: JSONObject =>
                    convertJsonToArray(eObject, fieldKey)
                  case value: String =>
                    Array(fieldKey -> value)
                }
            }
          case _ =>
            val value: String = if (tempVal == null) "" else tempVal.toString
            Array(fieldKey -> value)
        }
    }.toArray
  }
}
