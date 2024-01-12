package com.example.reviewboard.http.services

import com.example.reviewboard.http.domain.data.User
import zio.*
import zio.test.*

object JWTServiceSpec extends ZIOSpecDefault:
  override def spec =
    suite("JWTServiceSpec")(
      test("create and validate token") {
        for {
          service <- ZIO.service[JWTService]
          token   <- service.createToken(User(1L, "test@g.com", "test"))
          user    <- service.verifyToken(token.token)
        } yield assertTrue(user.id == 1L && user.email == "test@g.com")
      }
    ).provide(
      JWTServiceLive.configuredLayer
    )