package com.example.reviewboard.http.endpoints

import com.example.reviewboard.http.requests.*
import com.example.reviewboard.http.domain.data.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import zio.*

trait ReviewEndpoints extends BaseEndpoint:
  val createEndpoint = baseEndpoint
    .tag("create")
    .name("create")
    .description("Create listing of a review")
    .in("reviews")
    .post
    .in(jsonBody[CreateReviewRequest])
    .out(jsonBody[Review])

  val getByIdEndpoint = baseEndpoint
    .tag("getById")
    .name("getById")
    .description("Get by id")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint = baseEndpoint
    .tag("getByCompanyId")
    .name("getByCompanyId")
    .description("Get by Company id")
    .in("reviews" / "company" /path[Long]("id"))
    .get
    .out(jsonBody[List[Review]])

  val getByUserIdEndpoint = baseEndpoint
    .tag("getByUserId")
    .name("getByUserId")
    .description("Get by User id")
    .in("reviews" / "user" / path[Long]("id"))
    .get
    .out(jsonBody[List[Review]])

