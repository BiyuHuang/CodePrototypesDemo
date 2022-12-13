package com.wallace.demo.app.httpdemo

import com.wallace.demo.app.common.Using
import org.apache.http.Consts
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils

import scala.util.{Failure, Success, Try}

/**
  * Created by 10192057 on 2018/4/26 0026.
  */
object HttpUtils extends Using {

  private val httpClient: CloseableHttpClient = HttpClients.createDefault

  /**
    * 发送HttpGet请求
    *
    * @param url : String
    * @return
    */
  def sendGet(url: String, defaultRes: String = ""): String = {
    Try {
      val httpGet: HttpGet = new HttpGet(url)
      using(httpClient.execute(httpGet)) {
        response =>
          EntityUtils.toString(response.getEntity)
      }
    } match {
      case Success(res) => res
      case Failure(e) =>
        logger.error("Failed to do Post request: ", e)
        defaultRes
    }
  }


  /**
    * 发送HttpPost请求，无参数
    *
    * @param url : String
    * @return
    */
  def sendPost(url: String): String = {
    sendPost(url, params = "")
  }

  /**
    * 发送HttpPost请求，参数为Json
    *
    * @param url    : String
    * @param params : Json
    * @return
    */
  def sendPost(url: String, params: String, defaultRes: String = ""): String = {
    Try {
      val httpPost: HttpPost = new HttpPost(url)
      httpPost.setHeader("Accept", "application/json")
      httpPost.setHeader("Content-Type", "application/json")
      httpPost.setHeader("Authorization", "administrator:SdzV4wj1ufh3+X1PgIQXj7ld9gc=")
      if (params.nonEmpty) {
        val stingEntity: StringEntity = new StringEntity(params, Consts.UTF_8)
        httpPost.setEntity(stingEntity)
      }
      using(httpClient.execute(httpPost)) {
        response =>
          EntityUtils.toString(response.getEntity)
      }
    } match {
      case Success(res) => res
      case Failure(e) =>
        logger.error("Failed to do Post request: ", e)
        defaultRes
    }
  }
}
