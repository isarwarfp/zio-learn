package com.example

import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.{Task, *}
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint

object TapirDemo extends ZIOAppDefault:
  val simplestEndpoint = endpoint
    .tag("Simple Tag")
    .name("Simple")
    .description("Simplest Description")
    .get
    .in("simple")
    .out(plainBody[String])
    .serverLogicSuccess[Task](_ => ZIO.succeed("Done"))

  val simpleServerProgram = Server.serve(
    ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(simplestEndpoint)
  )

  // Part 2
  final case class Person(id: Long, name: String, age: Int)
  object Person:
    given jsonCodec: JsonCodec[Person] = DeriveJsonCodec.gen[Person]

  final case class CreatePersonRequest(name: String, age: Int)
  object CreatePersonRequest:
    given jsonCodec: JsonCodec[CreatePersonRequest] = DeriveJsonCodec.gen[CreatePersonRequest]


  var db: Map[Long, Person] = Map(1L -> Person(1L, "Imran", 20))
  val getAllEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("persons")
    .name("all")
    .description("Get all person")
    .in("all")
    .get
    .out(jsonBody[List[Person]])
    .serverLogicSuccess[Task](_ => ZIO.succeed(db.values.toList))

  val createPersonEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("create")
    .name("create")
    .description("Create a person")
    .in("persons")
    .post
    .in(jsonBody[CreatePersonRequest])
    .out(jsonBody[Person])
    .serverLogicSuccess(req => ZIO.succeed {
      val id = db.keys.max + 1
      val person = Person(id, req.name, req.age)
      db = db + (id -> person)
      person
    })

  val getByIdPersonEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("GetById")
    .name("GetById")
    .description("Get By Id Person")
    .in("persons" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Person]])
    .serverLogicSuccess(id => ZIO.succeed(db.get(id)))

  val personServerProgram = Server.serve(
    ZioHttpInterpreter(ZioHttpServerOptions.default)
      .toHttp(List(getAllEndpoint, createPersonEndpoint, getByIdPersonEndpoint))
  )


  override def run =
    //simpleServerProgram.provide(Server.default)
    personServerProgram.provide(Server.default)
