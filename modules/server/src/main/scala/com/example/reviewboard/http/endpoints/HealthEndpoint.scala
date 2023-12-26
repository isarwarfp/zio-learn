package com.example.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}

trait HealthEndpoint extends BaseEndpoint:
  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("Health")
    .description("Health Description")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint = baseEndpoint
    .tag("health")
    .name("Error Health")
    .description("Health check - should fail")
    .get
    .in("health" / "error")
    .out(plainBody[String])