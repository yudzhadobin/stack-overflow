package com.solar

import akka.actor.Props

object Main extends App {
  import Context.system

  val actor = system.actorOf(Props[AggregateActor])

  new WebServer(actor).startServer("localhost", 8080)

}
