/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.common.state

/**
  * Created by wallace on 2018/7/10.
  */

sealed trait State extends Serializable

case object Success extends State {
  val stateCode: Byte = 1
}

case object Failure extends State {
  val stateCode: Byte = 2
}

case object Aborted extends State {
  val stateCode: Byte = 3
}

case object Retried extends State {
  val stateCode: Byte = 4
}

case object Unknown extends State {
  val stateCode: Byte = 5
}