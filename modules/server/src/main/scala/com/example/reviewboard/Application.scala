package com.example.reviewboard

import com.example.reviewboard.http.HttpApi
import com.example.reviewboard.http.controllers.HealthController
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}
import com.example.reviewboard.http.services.*
import com.example.reviewboard.http.repositories.CompanyRepositoryLive
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import com.example.reviewboard.http.repositories.Repository
import com.example.reviewboard.http.repositories.ReviewRepositoryLive

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
    Repository.repoLayer
  )
