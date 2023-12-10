package com.example.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*

trait BaseController:
  val routes: List[ServerEndpoint[Any, Task]]
