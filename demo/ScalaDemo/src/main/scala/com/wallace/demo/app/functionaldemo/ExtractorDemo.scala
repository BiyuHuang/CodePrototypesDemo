package com.wallace.demo.app.functionaldemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2018/4/16 0016.
  */
object ExtractorDemo extends App with LogSupport {

  trait User {
    def name: String

    def score: Int
  }

  class FreeUser(val name: String, val score: Int, val upgradeProbability: Double) extends User

  class PremiumUser(val name: String, val score: Int) extends User

  object FreeUser {
    // Extractor
    def unapply(arg: FreeUser): Option[(String, Int, Double)] = Some((arg.name, arg.score, arg.upgradeProbability))
  }

  object PremiumUser {
    // Extractor
    def unapply(arg: PremiumUser): Option[(String, Int)] = Some((arg.name, arg.score))
  }

  object PremiumCandidate {
    // Extractor: return Boolean
    def unapply(arg: FreeUser): Boolean = arg.upgradeProbability > 0.75d
  }

  val user: User = new FreeUser("Daniel", 3000, 0.76d)
  user match {
    case FreeUser(name, _, p) =>
      if (p > 0.75) logger.info(name + ", what can we do for you today?") else logger.info("Hello " + name)
    case PremiumUser(name, _) => logger.info("Welcome back, dear " + name)
  }

  user match {
    case freeUser@PremiumCandidate() => logger.info(freeUser.name + ", what can we do for you today?")
    case _ => logger.info("Welcome back, Sir!")
  }
}
