/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.statics

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.DataFrame

/**
  * Created by wallace on 2019/6/5.
  * 线性特征归一化处理方法
  */
object LinearFeatureNormalized extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("MLLibDemo")) {
      spark =>
        val df: DataFrame = spark.createDataFrame(Seq(
          (0, Vectors.dense(1.0, 0.5, -1.0)),
          (1, Vectors.dense(2.0, 1.0, 1.0)),
          (2, Vectors.dense(5.0, 1.1, -4.0)),
          (3, Vectors.dense(3.0, 1.2, 1.9)),
          (4, Vectors.dense(10.0, 1.3, 4.0)),
          (5, Vectors.dense(4.0, 10.4, 2.0))
        )).toDF("id", "features")

        df.show(false)
        //TODO Normalizer
        /**
          * 其作用范围是每一行，使每一个行向量的范数变换为一个单位范数
          */

        // 正则化每个向量到1阶范数
        val normalizer: Normalizer = new Normalizer()
          .setInputCol("features")
          .setOutputCol("normFeatures")
          .setP(1.0)
        val l1NormData: DataFrame = normalizer.transform(df)
        println("Normalized using L^1 norm")
        l1NormData.show(false)

        // 正则化每个向量到无穷阶范数
        val lInfNormData: DataFrame = normalizer.transform(df, normalizer.p -> Double.PositiveInfinity)
        println("Normalized using L^inf norm")
        lInfNormData.show(false)

        //TODO StandardScaler
        /**
          * StandardScaler处理的对象是每一列，也就是每一维特征，将特征标准化为单位标准差或是0均值，或是0均值单位标准差。
          * 主要有两个参数可以设置：
          * withStd: 默认为真。将数据标准化到单位标准差。
          * withMean: 默认为假。是否变换为0均值。
          * StandardScaler需要fit数据，获取每一维的均值和标准差，来缩放每一维特征。
          */

        val scaler: StandardScaler = new StandardScaler()
          .setInputCol("features")
          .setOutputCol("scaledFeatures")
          .setWithStd(true)
          .setWithMean(false)

        // Compute summary statistics by fitting the StandardScaler.
        val scalerModel: StandardScalerModel = scaler.fit(df)

        // Normalize each feature to have unit standard deviation.
        val scaledData: DataFrame = scalerModel.transform(df)
        scaledData.show(false)

        //TODO MinMaxScaler
        /**
          * MinMaxScaler作用同样是每一列，即每一维特征。将每一维特征线性地映射到指定的区间，通常是[0, 1]。
          * 它也有两个参数可以设置：
          * min: 默认为0。指定区间的下限。
          * max: 默认为1。指定区间的上限。
          **/

        val minMaxScaler: MinMaxScaler = new MinMaxScaler()
          .setInputCol("features")
          .setOutputCol("scaledFeatures")

        // Compute summary statistics and generate MinMaxScalerModel
        val minMaxScalerModel: MinMaxScalerModel = minMaxScaler.fit(df)

        // rescale each feature to range [min, max].
        val minMaxScaledData: DataFrame = minMaxScalerModel.transform(df)
        println(s"Features scaled to range: [${minMaxScaler.getMin}, ${minMaxScaler.getMax}]")
        minMaxScaledData.select("features", "scaledFeatures").show(false)

        //TODO MaxAbsScaler
        /**
          * MaxAbsScaler将每一维的特征变换到[-1, 1]闭区间上，通过除以每一维特征上的最大的绝对值，
          * 它不会平移整个分布，也不会破坏原来每一个特征向量的稀疏性。
          **/
        val maxAbsScaler: MaxAbsScaler = new MaxAbsScaler()
          .setInputCol("features")
          .setOutputCol("scaledFeatures")

        // Compute summary statistics and generate MaxAbsScalerModel
        val maxAbsScalerModel: MaxAbsScalerModel = maxAbsScaler.fit(df)

        // rescale each feature to range [-1, 1]
        val maxAbsScaledData = maxAbsScalerModel.transform(df)
        maxAbsScaledData.select("features", "scaledFeatures").show(false)
    }
  }
}
