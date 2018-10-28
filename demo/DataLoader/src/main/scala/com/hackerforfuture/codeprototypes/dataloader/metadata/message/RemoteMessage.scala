/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.metadata.message

import com.hackerforfuture.codeprototypes.dataloader.metadata.EventType.EventType

/**
  * Created by wallace on 2018/10/8.
  */
sealed trait RemoteMessage

case class HeartBeat(data: String) extends RemoteMessage