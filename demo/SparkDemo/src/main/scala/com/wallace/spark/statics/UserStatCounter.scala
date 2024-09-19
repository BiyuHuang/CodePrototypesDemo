/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.statics

import org.apache.spark.util.StatCounter

/**
  * Created by wallace on 2019/5/30.
  */
class UserStatCounter(values: TraversableOnce[Double]) extends StatCounter(values) {
  //TODO Add calc mod method
  def mod: Double = Double.NaN

  override def toString: String = {
    "(count: %d, mean: %f, stdev: %f, max: %f, min: %f)".format(count, mean, stdev, max, min)
  }
}

object UserStatCounter {
  /** Build a UserStatCounter from a list of values. */
  def apply(values: TraversableOnce[Double]): UserStatCounter = new UserStatCounter(values)

  /** Build a UserStatCounter from a list of values passed as variable-length arguments. */
  def apply(values: Double*): UserStatCounter = new UserStatCounter(values)
}