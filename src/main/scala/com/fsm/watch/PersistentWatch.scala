package com.fsm.watch

import akka.actor._
import akka.persistence.PersistentActor

case class PersistentWatchInfo(watchState: WatchState, watch: Watch)

class PersistentWatch(id: String, initialWatchState: WatchState, initialWatch: Watch = DefaultWatch()) extends PersistentActor with WatchStateReceives with ActorLogging {

  override def persistenceId: String = id

  val info = PersistentWatchInfo(initialWatchState, initialWatch)

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = definedWatchStates(initialWatchState)(info)
}

object PersistentWatch {

  def props(id:String): Props = Props(new PersistentWatch(id, Default, DefaultWatch()))

  def addMinute(watch: Watch): DigitalWatch = {
    val minutes = watch.minutes + 1
    val (newHour, minute) = (minutes / 60 > 0, minutes % 60)

    val nuevaHora = DigitalWatch(watch.hours, minute)

    newHour match {
      case true => addHour(nuevaHora)
      case false => nuevaHora
    }
  }

  def addHour(watch: Watch): DigitalWatch = {
    val hour = (watch.hours + 1) % 24
    DigitalWatch(hour, watch.minutes)
  }

}