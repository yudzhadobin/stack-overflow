package com.solar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Context {
  implicit val system: ActorSystem = ActorSystem("parent-system")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val maxHttpConnectionCount: Int = 5
}
