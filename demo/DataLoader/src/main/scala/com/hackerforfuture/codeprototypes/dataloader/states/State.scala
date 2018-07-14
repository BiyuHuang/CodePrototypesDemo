package com.hackerforfuture.codeprototypes.dataloader.states

/**
  * Created by 10192057 on 2018/6/11 0011.
  */
sealed trait State[+T <: AnyVal] {
  def state: Option[T]
}

abstract class BooleanStates extends State[Boolean]

case object Failed extends BooleanStates {
  val state: Option[Boolean] = Some(false)
}

case object Succeed extends BooleanStates {
  val state: Option[Boolean] = Some(true)
}

case object Unknown extends BooleanStates {
  val state: Option[Boolean] = None
}

case class AntState() {
  @volatile var currentState: Option[Boolean] = Unknown.state

  def newState(nStat: BooleanStates): Unit = {
    this.currentState = nStat.state
  }
}

abstract class ByteStates extends State[Byte]

case object NotRunning extends ByteStates {
  val state: Option[Byte] = Option(0)
}

case object Starting extends ByteStates {
  val state: Option[Byte] = Option(1)
}

case object StableRunning extends ByteStates {
  val state: Option[Byte] = Option(2)
}

case object Pending extends ByteStates {
  val state: Option[Byte] = Option(3)
}

case object ShuttingDown extends ByteStates {
  val state: Option[Byte] = Option(4)
}

case object UnKnowState extends ByteStates {
  val state: Option[Byte] = Option(4)
}

case class WorkAntState() {
  @volatile var currentState: Option[Byte] = NotRunning.state

  def newState(nState: ByteStates): Unit = {
    this.currentState = nState.state
  }
}