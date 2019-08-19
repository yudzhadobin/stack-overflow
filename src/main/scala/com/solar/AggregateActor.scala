package com.solar

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.solar.AggregateActor.{Statistic, SearchMessage}
import com.solar.StackOverflowSearchActor.{SearchResults, StartSearchMessage}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class AggregateActor extends Actor {
  private implicit val timeout: Timeout = Timeout(10 seconds)
  private implicit val execContext: ExecutionContextExecutor = context.system.dispatcher
  private val actors = context.actorOf(RoundRobinPool(Context.maxHttpConnectionCount).props(Props[StackOverflowSearchActor]), "router2")

  override def receive: Receive = {
    case SearchMessage(tags) =>
      Future.traverse(tags){ tag =>
        actors.ask(StartSearchMessage(tag)).mapTo[SearchResults]
      }.map(answers => answers.flatMap(_.value)).map { searchResults =>
        searchResults.flatMap { case SearchResult(tags, isAnswered) =>
          tags.map(tag => tag -> isAnswered)
        }.groupBy { case (tag, _) => tag }.mapValues { seq => Statistic(seq.length, seq.count(_._2))}
      } pipeTo sender()
  }
}

object AggregateActor {
  case class SearchMessage(tags: Seq[String])

  case class Statistic(total: Int, answered: Int)
}