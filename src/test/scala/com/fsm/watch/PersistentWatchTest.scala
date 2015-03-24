package com.fsm.watch

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import akka.persistence.PersistentView
import org.scalatest.BeforeAndAfterEach
import utils.AkkaTest

case class EventRecoveredCount(count: Int)

class PersistentWatchTest extends AkkaTest with BeforeAndAfterEach {

  override def afterAll() = {
    system.shutdown()
  }

  val defaultWatch = DefaultWatch()
  val digitalWatch = DigitalWatch(0, 0)

  "Object PersistentWatch " must {

    import PersistentWatch._

    "agregar un minuto a Watch" in {
      addMinute(defaultWatch).minutes should be(1)
    }

    "volver a 0 cuando completa 60 minutos" in {
      addMinute(defaultWatch.copy(minutes = 59)).minutes should be(0)
    }

    "agregar una hora a Watch" in {
      addHour(defaultWatch).hours should be(1)
    }

    "volver a 0 cuando completa 24 horas" in {
      addHour(defaultWatch.copy(hours = 23)).hours should be(0)
    }

    "aumentar una hora a Watch cuando completa 60 minutos" in {
      val nuevaHora = addMinute(defaultWatch.copy(minutes = 59))
      nuevaHora.hours should be(1)
      nuevaHora.minutes should be(0)
    }
  }

  class Refresh(persistentWatchId: String = "persistent-watch-test-id", initialWatchState: WatchState = Default) {

    val pwActor = system.actorOf(Props(new PersistentWatch(persistentWatchId, initialWatchState)))

    def helperActor(receiveHelper: Receive)(task: => Unit): ActorRef = system.actorOf(Props(new Actor() {
      task

      override def receive: Receive = receiveHelper
    }))

    def viewHelper(): ActorRef = system.actorOf(Props(new PersistentView {

      override def viewId: String = s"$persistentWatchId-view"

      override def persistenceId: String = persistentWatchId

      var eventsRecovered = 0

      override def receive: Actor.Receive = {
        case e: EventRecoveredCount => sender() ! EventRecoveredCount(eventsRecovered)
        case e => eventsRecovered += 1
      }
    }))


  }

  def cambioEstado(watchStateInitial: WatchState, watchStateDestination: WatchState, watchEvent: WatchEvent, customInitialWatchState: Boolean = false) = {
    val initialState = if (customInitialWatchState) watchStateInitial else Default

    val prueba = if (watchStateInitial === watchStateDestination) "permanecer en el estado %s con el evento %s".format(watchStateInitial, watchEvent)
    else "cambiar del estado %s al estado %s con el evento %s".format(watchStateInitial, watchStateDestination, watchEvent)

    prueba in new Refresh(initialWatchState = initialState) {
      pwActor ! CurrentState
      expectMsg(watchStateInitial)

      pwActor ! watchEvent

      pwActor ! CurrentState
      expectMsg(watchStateDestination)

    }
  }

  "PersistentWatch" must {

    "iniciar con el estado %s al crearse el actor y con watch por default".format(Default) in new Refresh() {
      pwActor ! CurrentState
      expectMsg(Default)

      pwActor ! CurrentTime
      expectMsg(defaultWatch)
    }

    cambioEstado(Default, AdjustHours, Button1)

    cambioEstado(AdjustHours, AdjustMinutes, Button1, true)

    cambioEstado(AdjustMinutes, Normal, Button1, true)

    cambioEstado(Normal, AdjustHours, Button1, true)

    cambioEstado(AdjustHours, AdjustHours, Button2, true)

    cambioEstado(AdjustMinutes, AdjustMinutes, Button2, true)

    cambioEstado(Normal, Normal, Button2, true)


    "aumentar en 1 las horas en el estado AdjustHours con el evento Button2" in new Refresh(initialWatchState = AdjustHours) {
      pwActor ! CurrentState
      expectMsg(AdjustHours)

      pwActor ! Button2

      pwActor ! CurrentTime

      val watch: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch should be(DigitalWatch(1, 0))

    }

    "aumentar en 1 las horas en el estado AdjustHours con el evento Button2 y no ser mayor a 23" in new Refresh(initialWatchState = AdjustHours) {
      pwActor ! CurrentState
      expectMsg(AdjustHours)

      for (i <- 1 to 12) pwActor ! Button2

      pwActor ! CurrentTime

      val watch: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch should be(DigitalWatch(12, 0))


      for (i <- 1 to 12) pwActor ! Button2

      pwActor ! CurrentTime

      val watch2: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch2 should be(DigitalWatch(0, 0))

    }

    "aumentar en 1 los minutos en el estado AdjustMinutes con el evento Button2" in new Refresh(initialWatchState = AdjustMinutes) {
      pwActor ! CurrentState
      expectMsg(AdjustMinutes)

      pwActor ! Button2

      pwActor ! CurrentTime

      val watch: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch should be(DigitalWatch(0, 1))
    }


    "aumentar en 1 los minutos en el estado AdjustMinutes con el evento Button2 y no ser mayor de 60" in new Refresh(initialWatchState = AdjustMinutes) {
      pwActor ! CurrentState
      expectMsg(AdjustMinutes)

      for (i <- 1 to 30) pwActor ! Button2

      pwActor ! CurrentTime

      val watch: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch should be(DigitalWatch(0, 30))

      for (i <- 1 to 30) pwActor ! Button2

      pwActor ! CurrentTime

      val watch2: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch2.minutes should be(0)
      watch2 should be(DigitalWatch(1, 0))
    }

    "devolver un DigitalWatch en el estado Normal" in new Refresh(persistentWatchId = "persistent-test-new-name") {
      pwActor ! CurrentState
      expectMsg(Default)

      pwActor ! Button1
      pwActor ! CurrentState
      expectMsg(AdjustHours)

      pwActor ! Button1
      pwActor ! CurrentState
      expectMsg(AdjustMinutes)

      pwActor ! Button1
      pwActor ! CurrentState
      expectMsg(Normal)

      pwActor ! CurrentTime

      val watch: DigitalWatch = expectMsgPF() {
        case dw: DigitalWatch => dw
      }

      watch should be(DigitalWatch(0, 0))

      viewHelper() ! EventRecoveredCount(0)

      expectMsg(EventRecoveredCount(1))

    }

    "recuperar los eventos guardados por el persistentActor cuando cambia del estado Default a AdjustHours" in new Refresh(persistentWatchId = "persistent-test-new-name") {
      pwActor ! CurrentState
      expectMsg(Default)

      pwActor ! Button1
      pwActor ! Button1
      pwActor ! Button1
      pwActor ! Button1
      pwActor ! Button1
      pwActor ! Button1
      pwActor ! Button1

      pwActor ! CurrentState
      expectMsg(AdjustHours)

      viewHelper() ! EventRecoveredCount(0)

      expectMsg(EventRecoveredCount(2))

    }

  }
}
