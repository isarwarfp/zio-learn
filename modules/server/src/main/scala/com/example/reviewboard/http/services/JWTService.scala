package com.example.reviewboard.http.services

import zio.*
import com.auth0.jwt.*
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm

import java.time.Instant
import com.example.reviewboard.http.domain.data.*

trait JWTService:
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserId]

class JWTServiceLive(clock: java.time.Clock) extends JWTService:
  private val SECRET = "secret"
  private val ALGO = Algorithm.HMAC512(SECRET)
  private val ISSUER = "imran"
  private val TTL = 30 * 24 * 3600
  private val CLAIM_USER_NAME = "username"
  private val JWT_VERIFIER = JWT
    .require(ALGO)
    .withIssuer(ISSUER)
    .asInstanceOf[BaseVerification]
    .build(clock)

  def createToken(user: User): Task[UserToken] = for {
    now <- ZIO.attempt(clock.instant())
    expiration <- ZIO.succeed(now.plusSeconds(TTL))
    token <- ZIO.attempt(
      JWT
        .create()
        .withIssuer(ISSUER)
        .withIssuedAt(now)
        .withExpiresAt(expiration)
        .withSubject(user.id.toString)
        .withClaim(CLAIM_USER_NAME, user.email)
        .sign(ALGO)
    )
  } yield UserToken(user.email, token, expiration.getEpochSecond)
  def verifyToken(token: String): Task[UserId] = for {
    decoded <- ZIO.attempt(JWT_VERIFIER.verify(token))
    userId <- ZIO.attempt(
      UserId(decoded.getSubject().toLong, decoded.getClaim(CLAIM_USER_NAME).asString())
    )
  } yield userId

object JWTServiceLive:
  val layer = ZLayer {
    Clock.javaClock.map(new JWTServiceLive(_))
  }

object JWTServiceDemo extends ZIOAppDefault:
  val program = for {
    service <- ZIO.service[JWTService]
    token <- service.createToken(User(1, "imran", "not-important"))
    _ <- Console.printLine(token)
    userId <- service.verifyToken(token.token)
    _ <- Console.printLine(userId)
  } yield ()
  override def run = program.provide(JWTServiceLive.layer)




