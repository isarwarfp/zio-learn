package com.example.reviewboard.http.endpoints

import com.example.reviewboard.http.requests.RegisterUserAccount
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

trait UserEndpoints extends BaseEndpoint:
  val createUserEndpoint =
    baseEndpoint
    .tag("User")
    .name("Create User")
    .description("Create a new user")
    .in("users")
    .post
    .in(jsonBody[RegisterUserAccount])
