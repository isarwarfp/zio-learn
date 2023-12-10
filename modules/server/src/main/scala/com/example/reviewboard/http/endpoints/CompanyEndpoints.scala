package com.example.reviewboard.http.endpoints

import com.example.reviewboard.http.requests.*
import com.example.reviewboard.http.domain.data.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import zio.*

trait CompanyEndpoints:
  val createEndpoint = endpoint
    .tag("companies")
    .name("create")
    .description("Create listing of a company")
    .in("companies")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = endpoint
    .tag("companies")
    .name("getAll")
    .description("Get all companies")
    .in("companies")
    .get
    .out(jsonBody[List[Company]])

  val getByIdEndpoint = endpoint
    .tag("getById")
    .name("getById")
    .description("Get by id or slug")
    .in("companies" / path[String]("id"))
    .get
    .out(jsonBody[Option[Company]])
