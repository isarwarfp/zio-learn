package com.example.reviewboard.http.requests

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

final case class CreateReviewRequest(
  id: Long,
  companyId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
)

object CreateReviewRequest:
  given codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]