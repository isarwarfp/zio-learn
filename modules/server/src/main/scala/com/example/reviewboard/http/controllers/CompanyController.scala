package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.domain.data.Company
import com.example.reviewboard.http.endpoints.CompanyEndpoints
import com.example.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import zio.*
import com.example.reviewboard.http.services.CompanyService

class CompanyController private (service: CompanyService) 
extends BaseController with CompanyEndpoints:
  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogicSuccess { req => service.create(req) }
  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogicSuccess(_ => service.getAll )
  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogicSuccess { id => 
    ZIO.attempt(id.toLong)
    .flatMap(service.getById)
    .catchSome {
      case _: NumberFormatException => service.getBySlug(id)
    }
  }
  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)

object CompanyController:
  val mkZIO = for {
    service <- ZIO.service[CompanyService]
  } yield new CompanyController(service)
