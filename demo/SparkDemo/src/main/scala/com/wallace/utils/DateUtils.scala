/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.utils

import java.text.SimpleDateFormat
import java.util.Calendar

/**
  * Created by wallace on 2019/1/27.
  */
object DateUtils {

  private val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def addDate(srcDate: String, amount: Int, dateFormat: SimpleDateFormat = df): String = {
    val calc = Calendar.getInstance()
    calc.setTime(dateFormat.parse(srcDate))
    calc.add(Calendar.DATE, amount)
    df.format(calc.getTime)
  }
}
