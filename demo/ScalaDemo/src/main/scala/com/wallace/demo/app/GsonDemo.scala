package com.wallace.demo.app

import com.google.gson._
import com.wallace.demo.app.common.LogSupport
import org.slf4j.{Logger, LoggerFactory}

import java.lang.{Integer => JInt, Long => JLong}
import java.math.BigDecimal

/**
 * Author: wallace
 * Date: 2022/5/22 09:20
 * Description: Gson Demo
 */
class GsonDemo extends LogSupport {
  private val jsonStr: String = """{"key1":[{"key1_1":1000,"key1_2":11111111111111111},{"key1_1":2000,"key1_2":-123457}],"key2":[1,2,3,4,5,6,-1],"key3":-30,"key4":{"key4_1":-2,"key4_2":"test"}}"""
  private final val gson: Gson = new GsonBuilder().setPrettyPrinting().create()

  def formatJson(jsonStr: String): String = {
    gson.toJson(JsonParser.parseString(jsonStr))
  }

  def adjustJsonNode(jsonElement: JsonElement, jsonNodes: Map[String, String]): Unit = {
    if (jsonNodes.nonEmpty) {
      jsonNodes.foreach {
        case (jsonNodePath, dataType) =>
          val pathList: Array[String] = jsonNodePath.split("\\.")
          assert(pathList.nonEmpty, s"invalid Json path: $jsonNodePath")
          pathList.toList match {
            case parent :: Nil =>
              jsonElement match {
                case _: JsonPrimitive | _: JsonArray | _: JsonNull => // all of these cases should be ignored.
                case jsonObject: JsonObject =>
                  if (jsonObject.has(parent)) {
                    jsonObject.get(parent) match {
                      case _: JsonObject | _: JsonNull => // all of these cases should be ignored.
                      case jp: JsonPrimitive =>
                        val newValue: JsonPrimitive = correctNumeric(jp.getAsNumber, dataType)
                        // jsonObject.remove(parent)
                        jsonObject.add(parent, newValue)
                      case jsonArray: JsonArray =>
                        // only support Array[JsonPrimitive], can't be used for Array[JsonObject]
                        val newJsonArray: JsonArray = new JsonArray()
                        (0 until jsonArray.size()).foreach {
                          i: Int =>
                            val newValue: JsonPrimitive = correctNumeric(jsonArray.get(i).getAsNumber, dataType)
                            newJsonArray.add(newValue)
                        }
                        // jsonObject.remove(parent)
                        jsonObject.add(parent, newJsonArray)
                    }
                  }
              }
            case parent :: children =>
              val childrenNode: Map[String, String] = Map(children.mkString(".") -> dataType)
              jsonElement match {
                case _: JsonPrimitive | _: JsonNull | _: JsonArray => // all of these cases should be ignored.
                case jsonObject: JsonObject =>
                  if (jsonObject.has(parent)) {
                    jsonObject.get(parent) match {
                      case jsonArray: JsonArray =>
                        val newJsonArray = new JsonArray()
                        (0 until jsonArray.size()).foreach {
                          i: Int =>
                            adjustJsonNode(jsonArray.get(i), childrenNode)
                            newJsonArray.add(jsonArray.get(i))
                        }
                      case _: JsonObject =>
                        adjustJsonNode(jsonObject.get(parent), childrenNode)
                    }
                  }
              }
          }
      }
    }
  }

  def correctNumeric[T <: Number](data: T, dataType: String): JsonPrimitive = {
    dataType match {
      case "UINT32" =>
        new JsonPrimitive(JInt.toUnsignedLong(JInt.valueOf(data.intValue())))
      case "UINT64" =>
        new JsonPrimitive(new BigDecimal(JLong.toUnsignedString(JLong.valueOf(data.longValue()))))
      case _ =>
        throw new UnsupportedOperationException(s"unsupported operation for dataType: $dataType.")
    }
  }

  def run(): Unit = {
    val jsonObj: JsonElement = JsonParser.parseString(jsonStr)
    logger.info(jsonObj.getAsJsonObject.toString)

    val needFixNode: Map[String, String] = Map("key4.key4_1" -> "UINT32",
      "key1.key1_2" -> "UINT64",
      "key2" -> "UINT32",
      "key3" -> "UINT64",
      "not-existed-key" -> "UINT64")
    adjustJsonNode(jsonObj, needFixNode)

    logger.info(jsonObj.getAsJsonObject.toString)
    logger.info(s"formatted Json -> ${formatJson(jsonObj.toString)}")
  }
}

object GsonDemo {
  def main(args: Array[String]): Unit = {
    val gsonDemo = new GsonDemo
    gsonDemo.run()
  }
}
