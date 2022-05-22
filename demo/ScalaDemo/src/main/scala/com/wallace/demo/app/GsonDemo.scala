package com.wallace.demo.app

import com.google.gson.{JsonParser, JsonPrimitive}
import com.wallace.demo.app.common.LogSupport

import java.lang.{Long => JLong}
import java.math.BigDecimal

/**
 * Author: wallace
 * Date: 2022/5/22 09:20
 * Description: Gson Demo
 */
object GsonDemo extends LogSupport {

  private val jsonStr: String = """{"key1":[{"key1_1":1000,"key1_2":11111111111111111},{"key1_1":2000,"key1_2":-123457}],"key2":[1,2,3,4,5,6,-1]}"""
  private val jsonParser: JsonParser = new JsonParser()

  def main(args: Array[String]): Unit = {
    val jsonObj = jsonParser.parse(jsonStr)
    val unsignedInt64: BigDecimal = new BigDecimal(JLong.toUnsignedString(JLong.valueOf(-1L)))
    jsonObj.getAsJsonObject.add("key3", new JsonPrimitive(unsignedInt64))

    log.info(jsonObj.getAsJsonObject.toString)
  }
}
