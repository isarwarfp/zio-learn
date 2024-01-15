package com.example.reviewboard

import com.example.reviewboard.config.JWTConfig
import com.example.reviewboard.config.configs.mkLayer
import com.example.reviewboard.http.HttpApi
import com.example.reviewboard.http.controllers.HealthController
import com.example.reviewboard.http.repositories.{CompanyRepositoryLive, Repository, ReviewRepositoryLive, UserRepositoryLive}
import com.example.reviewboard.http.services.*
import com.stripe.param.ChargeUpdateParams.FraudDetails.UserReport
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}

object Application extends ZIOAppDefault:

  val serverProgram = for {
    routes <- HttpApi.routes
    server <- Server.serve(ZioHttpInterpreter(
      ZioHttpServerOptions.default)
      .toHttp(routes))
    _          <- Console.printLine("Server Started !")
  } yield ()

  override def run = serverProgram.provide(
    Server.default,
    CompanyRepositoryLive.layer,
    CompanyServiceLive.layer,
    ReviewRepositoryLive.layer,
    ReviewServiceLive.layer,
    Repository.repoLayer,
    UserRepositoryLive.layer,
    JWTServiceLive.configuredLayer,
    UserServiceLive.layer
  )
