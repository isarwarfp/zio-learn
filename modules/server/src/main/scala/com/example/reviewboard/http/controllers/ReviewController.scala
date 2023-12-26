package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.endpoints.ReviewEndpoints
import com.example.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import zio.*
import com.example.reviewboard.http.services.ReviewService

class ReviewController private (service: ReviewService) extends BaseController with ReviewEndpoints:
  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { req => service.create(req, -1L).either }
  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>  service.getById(id).either }
  val getByCompanyId: ServerEndpoint[Any, Task] = getByCompanyIdEndpoint.serverLogic { id =>  service.getByCompanyId(id).either }
  val getByUserId: ServerEndpoint[Any, Task] = getByUserIdEndpoint.serverLogic { id =>  service.getByUserId(id).either }
  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId, getByUserId)

object ReviewController:
  val mkZIO = ZIO.service[ReviewService].map(service => new ReviewController(service))
