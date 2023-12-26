package com.example.reviewboard.http.domain.error

import sttp.model.StatusCode

final case class HttpError(
  statusCode: StatusCode,
  message: String,
  cause: Throwable
) extends RuntimeException(message, cause)

object HttpError:
  def decode(tuple: (StatusCode, String)) =
    HttpError(tuple._1, tuple._2, new RuntimeException(tuple._2))
  
    // Commented because of endpoint would return Task[Either[Throwable, String]]
  // def encode(error: HttpError) =
  //   (error.statusCode, error.message)
  def encode(error: Throwable) =
    (StatusCode.InternalServerError, error.getMessage)

