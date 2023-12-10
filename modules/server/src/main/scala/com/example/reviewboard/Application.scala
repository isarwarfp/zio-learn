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
import com.example.reviewboard.http.services.CompanyService

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
    CompanyService.serviceLayer
  )
