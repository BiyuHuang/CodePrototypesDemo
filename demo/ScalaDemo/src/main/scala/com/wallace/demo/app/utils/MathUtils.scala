/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.nio.ByteBuffer

/**
  * Created by 10192057 on 2018/4/17 0017.
  */
object MathUtils {
  def byteArrayToInt(byteArray: Array[Byte]): Int = {
    // ByteOrder.LITTLE_ENDIAN
    assert(byteArray.length == 4, "Int value must be 32 bits / 4 Bytes")
    byteArray(3) & 0xFF |
      (byteArray(2) & 0xFF) << 8 |
      (byteArray(1) & 0xFF) << 16 |
      (byteArray(0) & 0xFF) << 24
  }

  def intToByteArray(value: Int): Array[Byte] = {
    assert(value <= Int.MaxValue, s"Int value must be less than ${Int.MaxValue}.")
    Array[Byte](
      ((value >> 24) & 0xFF).toByte,
      ((value >> 16) & 0xFF).toByte,
      ((value >> 8) & 0xFF).toByte,
      (value & 0xFF).toByte)
  }

  def longToByteArray(value: Long): Array[Byte] = {
    assert(value <= Long.MaxValue, s"Long value must be less than ${Long.MaxValue}.")
    val buffer: ByteBuffer = ByteBuffer.allocate(8)
    buffer.putLong(0, value)
    buffer.array()
  }

  def byteArrayToLong(byteArray: Array[Byte]): Long = {
    assert(byteArray.length == 8, "Long value must be 64 bits / 8 Bytes")
    val buffer: ByteBuffer = ByteBuffer.allocate(8)
    buffer.put(byteArray)
    buffer.flip()
    buffer.getLong
  }

  def parseInt(in: String, radio: String): Int = {
    radio match {
      case "d" => parseInt(in, 10)
      case "h" => parseInt(in, 16)
      case "o" => parseInt(in, 8)
      case "b" => parseInt(in, 2)
      case _ => parseInt(in, 10)
    }
  }

  def parseInt(in: String, radio: Int): Int = Integer.parseInt(in, radio)

  def formatValue(in: Int, radio: String): String = {
    radio match {
      case "d" => Integer.toString(in)
      case "h" => Integer.toHexString(in)
      case "o" => Integer.toOctalString(in)
      case "b" => Integer.toBinaryString(in)
      case _ => in.toString
    }
  }

  def formatValue(in: Double, radio: String): String = formatValue(in.toInt, radio)

  def execOperations(value: Int, operator: String, operand: Int): Int = {
    operator match {
      case "+" => value + operand
      case "-" => value - operand
      case "*" => if (operand != 0) value * operand else value
      case "/" => if (operand != 0) value / operand else value
      case "%" => if (operand != 0) value % operand else value
    }
  }

  def execOperations(value: Double, operator: String, operand: Double): Double = {
    operator match {
      case "+" => value + operand
      case "-" => value - operand
      case "*" => if (operand != 0) value * operand else value
      case "/" => if (operand != 0) value / operand else value
      case "%" => if (operand != 0) value % operand else value
    }
  }

  def getSumOfLong(x: Long): Long = (0 to 63).map(x >> _ & 0x01).sum


  def oddEvenCheck(x: Int): Int = x match {
    case 0 => 0
    case _ => x & 0x01
  }
}
