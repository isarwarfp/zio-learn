package com.example.reviewboard.http

import com.example.reviewboard.http.controllers.*
import com.example.reviewboard.http.services.CompanyService

object HttpApi:
  private def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  private def mkController = for {
    health    <- HealthController.mkZIO
    companies <- CompanyController.mkZIO
    reviews   <- ReviewController.mkZIO
    users     <- UserController.mkZIO
  } yield List(health, companies, reviews, users)

  def routes = mkController.map(gatherRoutes)
