package com.example.reviewboard.http.integration

import com.example.reviewboard.config.JWTConfig
import com.example.reviewboard.http.controllers.ReviewControllerSpec.review
import com.example.reviewboard.http.controllers.UserController
import com.example.reviewboard.http.requests.{CreateReviewRequest, LoginRequest, RegisterUserAccount}
import sttp.client3.UriContext
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import sttp.monad.syntax.*
import sttp.client3.*
import sttp.tapir.generic.auto.*
import com.example.reviewboard.http.controllers.*
import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.repositories.{Repository, RepositorySpec, UserRepositoryLive}
import com.example.reviewboard.http.responses.UserResponse
import com.example.reviewboard.http.services.{JWTServiceLive, UserServiceLive}
import sttp.model.Method
import zio.*
import zio.test.*
import zio.json.*

object UserFlowISpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript: String = "sql/integration.sql"

  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  extension[A: JsonCodec] (backend: SttpBackend[Task, Nothing]) {
    def sendRequest[B: JsonCodec](
        method: Method,
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"$path")
        .auth.bearer(maybeToken.getOrElse(""))
        .body(payload.toJson)
        .send(backend)
        .map(_.body)
        .map(_.flatMap(json => json.fromJson[B]).toOption)

    def post[B: JsonCodec](path: String, payload: A): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, None)
  }

  private def backendStubZio =
    for {
      controller <- UserController.mkZIO
      backend    <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointsRunLogic(controller.routes)
          .backend()
      )
    } yield backend

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserFlowISpec")(
      test("create user") {
        for {
          backendStub   <- backendStubZio
          maybeResponse <- backendStub.post[UserResponse] (
            "/users",
            RegisterUserAccount(email = "isarwar@gmail.com", password = "test")
          )
        } yield assertTrue(maybeResponse.nonEmpty)
      },
      test("create user and log in") {
        for {
          backendStub <- backendStubZio
          maybeResponse <- backendStub.post[UserResponse] (
            "/users",
            RegisterUserAccount(email = "isarwar@gmail.com", password = "test")
          )
          maybeToken <- backendStub.post[UserToken] (
            "/users/login",
            LoginRequest(email = "isarwar@gmail.com", password = "test")
          )
        } yield
          assertTrue(maybeToken.filter(_.email == "isarwar@gmail.com").nonEmpty)
      }
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      UserRepositoryLive.layer,
      Repository.quillLayer,
      dsLayer,
      ZLayer.succeed(JWTConfig("secret", 3600)),
      Scope.default
    )
