package com.fsm.watch

sealed trait WatchState

final case object Default extends WatchState
final case object Normal extends WatchState
final case object AdjustHours extends WatchState
final case object AdjustMinutes extends WatchState

sealed trait Watch {
  val hours: Int
  val minutes: Int
}

final case class DefaultWatch(hours: Int = 0, minutes: Int = 0) extends Watch
final case class DigitalWatch(hours: Int, minutes: Int) extends Watch

sealed trait WatchEvent

final case object Button1 extends WatchEvent
final case object Button2 extends WatchEvent
final case object Button3 extends WatchEvent

final case object CurrentState extends WatchEvent
final case object CurrentTime extends WatchEvent
