package com.wallace.demo.app.implicitdemo

/**
  * Created by 10192057 on 2018/8/17 0017.
  */
class ImplicitDemo {

}

class SwingType {
  def wantLearned(sw: String): Unit = println("兔子已经学会了" + sw)
}

object Swimming {
  implicit def learningType(s: AnimalType): SwingType = new SwingType
}

class AnimalType

object AnimalType {
  def main(args: Array[String]): Unit = {
    //TODO 隐式视图
    import com.wallace.demo.app.implicitdemo.Swimming._
    val rabbit: AnimalType = new AnimalType
    rabbit.wantLearned("breaststroke") //蛙泳

    //TODO 隐式类
    import com.wallace.demo.app.implicitdemo.StringUtils._
    println("mobin".increment)
  }
}


object StringUtils {

  implicit class StringImprovement(val s: String) { //隐式类
    def increment: String = s.map(x => (x + 1).toChar)
  }

}