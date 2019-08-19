import MainActor.{Answer, SearchMessage}
import StackOverflowSearchActor.{SearchResults, StartSearchMessage}
import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class MainActor extends Actor {
  private implicit val timeout: Timeout = Timeout(10 seconds)
  private implicit val execContext: ExecutionContextExecutor = context.system.dispatcher


  private val actors = context.actorOf(RoundRobinPool(5).props(Props[StackOverflowSearchActor]), "router2")

  override def receive: Receive = {
    case SearchMessage(tags) =>
      Future.traverse(tags){ tag =>
        actors.ask(StartSearchMessage(tag)).mapTo[SearchResults]
      }.map(answers => answers.flatMap(_.value)).map { searchResults =>
        searchResults.flatMap { case SearchResult(tags, isAnswered) =>
          tags.map(tag => tag -> isAnswered)
        }.groupBy { case (tag, _) => tag }.mapValues { seq => Answer(seq.length, seq.count(_._2))}
      } pipeTo sender()
  }
}

object MainActor {
  case class SearchMessage(tags: Seq[String])

  case class Answer(total: Int, answered: Int)
}