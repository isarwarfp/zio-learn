package com.example.reviewboard.http.endpoints

import com.example.reviewboard.http.requests.*
import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.responses.UserResponse
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
    .out(jsonBody[UserResponse])

  // TODO: Add authentication
  val updatePasswordEndpoint =
    secureEndpoint
    .tag("User")
    .name("Update Password")
    .description("Update a user's password")
    .in("users" / "password")
    .in(jsonBody[UpdatePasswordRequest])
    .put
    .out(jsonBody[UserResponse])

  // TODO: Add authentication
  val deleteUserEndpoint =
    secureEndpoint
    .tag("User")
    .name("Delete User")
    .description("Delete a user")
    .in("users")
    .in(jsonBody[DeleteAccountRequest])
    .delete
    .out(jsonBody[UserResponse])

  val loginEndpoint =
    baseEndpoint
    .tag("User")
    .name("Login")
    .description("Login to an account")
    .in("users" / "login")
    .post
    .in(jsonBody[LoginRequest])
    .out(jsonBody[UserToken])