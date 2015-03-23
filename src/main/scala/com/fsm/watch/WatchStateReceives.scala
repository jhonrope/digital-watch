package com.fsm.watch

import akka.actor.Actor

import scala.collection.immutable.HashMap

trait WatchStateReceives {
   this: Actor =>

import PersistentWatch._

   lazy val _intermediaryMap: Map[WatchState, PersistentWatchInfo => Receive] = HashMap(Default -> defaultReceive _,
     AdjustHours -> adjustHoursReceive _,
     AdjustMinutes -> adjustMinutesReceive _,
     Normal -> normalReceive _
   )

   lazy val definedWatchStates: Map[WatchState, PersistentWatchInfo => Receive] = _intermediaryMap.withDefaultValue(defaultReceive _)

   def defaultReceive(pwi: PersistentWatchInfo): Receive = {
     val innerReceive: Receive = {
       case Button1 => context.become(adjustHoursReceive(pwi.copy(watchState = AdjustHours)))
     }
     innerReceive orElse commonReceive(pwi)
   }

   def adjustHoursReceive(pwi: PersistentWatchInfo): Receive = {
     val innerReceive: Receive = {
       case Button1 => context.become(adjustMinutesReceive(pwi.copy(watchState = AdjustMinutes)))
       case Button2 =>
         val operacion = pwi.copy(watch = addHour(pwi.watch))
         context.become(adjustHoursReceive(operacion))
     }
     innerReceive orElse commonReceive(pwi)
   }

   def adjustMinutesReceive(pwi: PersistentWatchInfo): Receive = {
     val innerReceive: Receive = {
       case Button1 =>
         val watch = pwi.watch
         val operacion = pwi.copy(watchState = Normal, DigitalWatch(watch.hours, watch.minutes))
         context.become(normalReceive(operacion))
       case Button2 =>
         val operacion = pwi.copy(watch = addMinute(pwi.watch))
         context.become(adjustMinutesReceive(operacion))
     }
     innerReceive orElse commonReceive(pwi)
   }

   def normalReceive(pwi: PersistentWatchInfo): Receive = {
     val innerReceive: Receive = {
       case Button1 => context.become(adjustHoursReceive(pwi.copy(watchState = AdjustHours)))
     }
     innerReceive orElse commonReceive(pwi)
   }

   def commonReceive(pwi: PersistentWatchInfo): Receive = {
     case CurrentState => sender() ! pwi.watchState
     case CurrentTime => sender() ! pwi.watch
   }
 }
