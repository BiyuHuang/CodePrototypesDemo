package com.wallace.spark.sparkstreaming

import com.wallace.common.LogSupport

/**
  * com.wallace.spark.sparkstreaming
  * Created by 10192057 on 2017/12/23 0023.
  */
class PIDRateEstimator(batchIntervalMillis: Long,
                       proportional: Double,
                       integral: Double,
                       derivative: Double,
                       minRate: Double) extends LogSupport {
  private var firstRun: Boolean = true
  private var latestTime: Long = -1L
  private var latestRate: Double = -1D
  private var latestError: Double = -1L

  require(
    batchIntervalMillis > 0,
    s"Specified batch interval $batchIntervalMillis in PIDRateEstimator is invalid.")
  require(
    proportional >= 0,
    s"Proportional term $proportional in PIDRateEstimator should be >= 0.")
  require(
    integral >= 0,
    s"Integral term $integral in PIDRateEstimator should be >= 0.")
  require(
    derivative >= 0,
    s"Derivative term $derivative in PIDRateEstimator should be >= 0.")
  require(
    minRate > 0,
    s"Minimum rate in PIDRateEstimator should be > 0")

  log.info(s"Created PIDRateEstimator with proportional = $proportional, integral = $integral, " +
    s"derivative = $derivative, min rate = $minRate")

  def compute(
               time: Long, // in milliseconds
               numElements: Long,
               processingDelay: Long, // in milliseconds
               schedulingDelay: Long // in milliseconds
             ): Option[Double] = {
    log.debug(s"\ntime = $time, # records = $numElements, " +
      s"processing time = $processingDelay, scheduling delay = $schedulingDelay")
    this.synchronized {
      if (time > latestTime && numElements > 0 && processingDelay > 0) {

        // in seconds, should be close to batchDuration
        val delaySinceUpdate = (time - latestTime).toDouble / 1000

        // in elements/second
        val processingRate = numElements.toDouble / processingDelay * 1000

        // In our system `error` is the difference between the desired rate and the measured rate
        // based on the latest batch information. We consider the desired rate to be latest rate,
        // which is what this estimator calculated for the previous batch.
        // in elements/second
        val error = latestRate - processingRate

        // The error integral, based on schedulingDelay as an indicator for accumulated errors.
        // A scheduling delay s corresponds to s * processingRate overflowing elements. Those
        // are elements that couldn't be processed in previous batches, leading to this delay.
        // In the following, we assume the processingRate didn't change too much.
        // From the number of overflowing elements we can calculate the rate at which they would be
        // processed by dividing it by the batch interval. This rate is our "historical" error,
        // or integral part, since if we subtracted this rate from the previous "calculated rate",
        // there wouldn't have been any overflowing elements, and the scheduling delay would have
        // been zero.
        // (in elements/second)
        val historicalError = schedulingDelay.toDouble * processingRate / batchIntervalMillis

        // in elements/(second ^ 2)
        val dError = (error - latestError) / delaySinceUpdate

        val newRate = (latestRate - proportional * error -
          integral * historicalError -
          derivative * dError).max(minRate)
        log.debug(
          s"""
             | latestRate = $latestRate, error = $error
             | latestError = $latestError, historicalError = $historicalError
             | delaySinceUpdate = $delaySinceUpdate, dError = $dError
            """.stripMargin)

        latestTime = time
        if (firstRun) {
          latestRate = processingRate
          latestError = 0D
          firstRun = false
          log.debug("First run, rate estimation skipped")
          None
        } else {
          latestRate = newRate
          latestError = error
          log.debug(s"New rate = $newRate")
          Some(newRate)
        }
      } else {
        log.debug("Rate estimation skipped")
        None
      }
    }
  }
}
