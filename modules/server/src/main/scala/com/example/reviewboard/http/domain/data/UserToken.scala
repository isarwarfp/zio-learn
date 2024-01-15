package com.example.reviewboard.http.domain.data

import zio.json.*

final case class UserToken(
  email: String,
  token: String,
  expires: Long
) derives JsonCodec
