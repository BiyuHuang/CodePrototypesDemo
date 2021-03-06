///*
// * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
// * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
// * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
// * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
// * Vestibulum commodo. Ut rhoncus gravida arcu.
// */
//
//package com.wallace.spark.sparkmllibdemo
//
//import com.microsoft.ml.spark.LightGBMClassificationModel
//import com.wallace.common.{CreateSparkSession, Using}
//import org.apache.spark.mllib.tree.configuration.BoostingStrategy
//
//import scala.collection.immutable.TreeMap
//
///**
//  * Created by wallace on 2019/7/23.
//  */
//object LightGBMDemo extends CreateSparkSession with Using {
//  def main(args: Array[String]): Unit = {
//    usingSpark(createSparkSession("LightGBM_Demo")) {
//      spark =>
//
//        val lightGBMModel = LightGBMClassificationModel.loadNativeModelFromFile("")
//
//        lightGBMModel
//          .setFeaturesCol("")
//          .setPredictionCol("")
//          .getProbabilityCol
//
//
//        val tMap = new TreeMap[Int, Int]()(Ordering[Int])
//        val bStrategy = BoostingStrategy.defaultParams("Classification")
//        bStrategy.setLearningRate(0.8)
//        bStrategy.setNumIterations(100)
//    }
//  }
//}
