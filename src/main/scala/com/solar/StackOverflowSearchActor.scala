package com.solar

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import com.solar.StackOverflowSearchActor.{SearchResults, StartSearchMessage}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Json}

import scala.concurrent.ExecutionContextExecutor

class StackOverflowSearchActor extends Actor {
  private implicit val execContext: ExecutionContextExecutor = context.system.dispatcher
  import Context.materializer

  override def receive: Receive = {
    case StartSearchMessage(tag) =>
      val decoder = implicitly[Decoder[Seq[SearchResult]]]
      Http()(context.system).singleRequest(
        createRequest(tag)
      ).map(Gzip.decodeMessage(_)).flatMap(Unmarshal(_).to[Json]).map(value =>
        decoder.tryDecode(value.hcursor.downField("items")) match {
          case Right(v) => v
          case Left(e) => throw e
        }
      ).map(SearchResults.apply) pipeTo sender()
  }


  def createRequest(tag: String) : HttpRequest = {
    HttpRequest(
      uri = s"https://api.stackexchange.com/2.2/search?pagesize=100&order=desc&sort=creation&tagged=$tag&site=stackoverflow",
      headers = List(Accept(MediaTypes.`application/json`))
    )
  }
}

object StackOverflowSearchActor {
  case class StartSearchMessage(tag: String)

  case class SearchResults(value: Seq[SearchResult])
}