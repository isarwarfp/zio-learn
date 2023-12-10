package com.example.reviewboard.http

import com.example.reviewboard.http.controllers.{BaseController, CompanyController, HealthController}
import com.example.reviewboard.http.services.CompanyService

object HttpApi:
  private def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  private def mkController = for {
    health    <- HealthController.mkZIO
    companies <- CompanyController.mkZIO
  } yield List(health, companies)

  def routes = mkController.map(gatherRoutes)
