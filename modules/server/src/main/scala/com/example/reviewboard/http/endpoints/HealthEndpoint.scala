package com.example.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}

trait HealthEndpoint:
  val healthEndpoint = endpoint
    .tag("health")
    .name("Health")
    .description("Health Description")
    .get
    .in("health")
    .out(plainBody[String])