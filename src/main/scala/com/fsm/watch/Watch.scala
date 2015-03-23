package com.fsm.watch

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Created by Jhony on 23/03/2015.
 */
object WatchApp extends App {

  val system = ActorSystem("watch")

  val watch = system.actorOf(PersistentWatch.props(), "persistent-actor-1")
  val duration = FiniteDuration(1, "second")
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(1000, MILLISECONDS)

  system.actorOf(Props(new Actor(){

    watch ! Button1
    watch ! Button1
    watch ! Button2
    watch ! Button1
    watch ! CurrentTime
    watch ! CurrentState

    override def receive: Receive = {
      case e => print(e)
    }
  }))

  Thread.sleep(1000)
  system.shutdown()
}
