package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.*
import com.example.reviewboard.http.domain.error.HttpError

class HealthController extends BaseController with HealthEndpoint:
  val health: ServerEndpoint[Any, Task] = healthEndpoint.serverLogicSuccess[Task](_ => ZIO.succeed("Done"))

  val error = errorEndpoint
  .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Health failed")).either)

  override val routes: List[ServerEndpoint[Any, Task]] = List(health, error)

object HealthController:
  val mkZIO = ZIO.succeed(new HealthController)