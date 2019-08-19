package com.solar

import io.circe.Decoder

case class SearchResult(tags: Seq[String], isAnswered: Boolean)

case object SearchResult {
  implicit val decodeUser: Decoder[SearchResult] =
    Decoder.forProduct2("tags", "is_answered")(SearchResult.apply)
}
