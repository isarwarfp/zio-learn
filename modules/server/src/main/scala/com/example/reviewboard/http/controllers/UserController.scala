package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.domain.data.UserId
import com.example.reviewboard.http.domain.error.*
import com.example.reviewboard.http.endpoints.UserEndpoints
import com.example.reviewboard.http.responses.UserResponse
import com.example.reviewboard.http.services.*
import zio.*
import sttp.tapir.*
import sttp.tapir.server.*

class UserController private (userService: UserService, jwtService: JWTService) extends BaseController with UserEndpoints:
  val create: ServerEndpoint[Any, Task] = createUserEndpoint.serverLogic { req =>
    userService.registerUser(req.email, req.password)
      .map(u => UserResponse(u.email))
      .either
  }

  val login: ServerEndpoint[Any, Task] = loginEndpoint
    .serverLogic { req =>
      userService
      .generateToken(req.email, req.password)
      .someOrFail(UnauthorizedException)
      .either
  }

  val updatePassword: ServerEndpoint[Any, Task] = updatePasswordEndpoint
    .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { userId => req =>
      userService.updatePassword(req.email, req.oldPassword, req.newPassword)
        .map(u => UserResponse(u.email)) 
        .either
    }

  val deleteUser: ServerEndpoint[Any, Task] = deleteUserEndpoint
    .serverSecurityLogic[UserId, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { userId => req =>
      userService.deleteUser(req.email, req.password)
        .map(u => UserResponse(u.email))
        .either
    }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    login,
    updatePassword,
    deleteUser
  )

object UserController:
  val mkZIO = for {
    userService <- ZIO.service[UserService]
    jwtService  <- ZIO.service[JWTService]
  } yield new UserController(userService, jwtService)