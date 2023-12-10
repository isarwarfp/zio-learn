package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController extends BaseController with HealthEndpoint:
  val health: ServerEndpoint[Any, Task] = healthEndpoint.serverLogicSuccess[Task](_ => ZIO.succeed("Done"))

  override val routes: List[ServerEndpoint[Any, Task]] = List(health)

object HealthController:
  val mkZIO = ZIO.succeed(new HealthController)