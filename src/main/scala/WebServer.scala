import MainActor.Answer
import akka.actor.{ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import io.circe._, io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Printer

class WebServer(mainActor: ActorRef) extends HttpApp {
  private implicit val timeout: Timeout = Timeout(60 seconds)

  private implicit val printer: Printer = Printer.spaces2

  override protected def routes: Route = (path("search") & get) {
    parameters('tag.*) { tags =>
      onComplete((mainActor ? MainActor.SearchMessage(tags.toSeq)).mapTo[Map[String, Answer]]) {
        case Success(value) => complete(StatusCodes.OK, value)
        case Failure(e) => complete(StatusCodes.InternalServerError, e)
      }
    }
  }
}