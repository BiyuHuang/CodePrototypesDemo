/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.persist

import com.hackerforfuture.codeprototypes.dataloader.common.persist.PersistWriter
import com.hackerforfuture.codeprototypes.dataloader.common.state.State

import scala.concurrent.Future

/**
  * Created by wallace on 2018/7/11.
  */
class FtpPersistWriter extends PersistWriter {
  override def configure(func: () => State): State = super.configure(func)
  override def write(): Future[State] = {
    ???
  }
}
