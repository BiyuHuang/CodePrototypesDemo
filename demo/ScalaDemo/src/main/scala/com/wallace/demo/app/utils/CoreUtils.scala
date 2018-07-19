package com.wallace.demo.app.utils

import java.io.EOFException
import java.nio.{ByteBuffer, ByteOrder}
import java.nio.channels.ReadableByteChannel

/**
  * Created by 10192057 on 2018/6/8 0008.
  */
object CoreUtils {
  def runnable(fun: => Unit): Runnable =
    new Runnable {
      override def run(): Unit = fun
    }

  def read(channel: ReadableByteChannel, buffer: ByteBuffer): Int = {
    channel.read(buffer) match {
      case -1 => throw new EOFException("Received -1 when reading from channel, socket has likely been closed.")
      case n: Int => n
    }
  }

  /**
    * Read a big-endian integer from a byte array
    */
  def readInt(bytes: Array[Byte], offset: Int): Int = {
    ((bytes(offset) & 0xFF) << 24) |
      ((bytes(offset + 1) & 0xFF) << 16) |
      ((bytes(offset + 2) & 0xFF) << 8) |
      (bytes(offset + 3) & 0xFF)
  }
}
