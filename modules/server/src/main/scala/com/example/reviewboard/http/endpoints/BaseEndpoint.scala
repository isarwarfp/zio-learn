package com.example.reviewboard.http.endpoints

import sttp.tapir.*
import com.example.reviewboard.http.domain.error.HttpError

trait BaseEndpoint:
  val baseEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)

  val secureEndpoint = baseEndpoint.securityIn(auth.bearer[String]())
