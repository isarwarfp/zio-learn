package com.example.reviewboard.http.controllers

import com.example.reviewboard.http.requests.CreateCompanyRequest
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.test.*
import sttp.client3.*
import zio.json.*
import sttp.tapir.generic.auto.*
import com.example.reviewboard.http.controllers.CompanyController
import com.example.reviewboard.http.domain.data.Company
import com.example.reviewboard.http.services.CompanyService

import scala.tools.nsc.interactive.Pickler.TildeDecorator
import sttp.tapir.server.ServerEndpoint
import com.example.reviewboard.syntax.*

object CompanyControllerSpec extends ZIOSpecDefault:

  private given zioME: MonadError[Task] = new RIOMonadError[Any]
  private val company = Company(1L, "my-company", "My Company", "www.go.com")
  private val serviceStub = new CompanyService {
    override def create(req: CreateCompanyRequest): Task[Company] = ZIO.succeed(company)
    override def getAll: Task[List[Company]] = ZIO.succeed(List(company))
    override def getById(id: Long): Task[Option[Company]] = 
      ZIO.succeed(if(id == 1) (Some(company)) else None)
    override def getBySlug(slug: String): Task[Option[Company]] = 
      ZIO.succeed(if(slug == "my-company") Some(company) else None)
  }

  private def backendStubZIO(endpoint: CompanyController => ServerEndpoint[Any, Task]) = for {
    // Create the controller
    controller <- CompanyController.mkZIO
    // Build Tapir backend
    backendStub <- ZIO.succeed(TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpoint(controller)).backend())
  } yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post company") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          // run http request
          response <- basicRequest
            .post(uri"/companies")
            .body(CreateCompanyRequest("My Company", "www.go.com").toJson)
            .send(backendStub)
        } yield response.body
        // inspect http response
        program.assert { respBody =>
          respBody.toOption.flatMap(_.fromJson[Company].toOption)
          .contains(Company(1, "my-company", "My Company", "www.go.com"))
        }
      },
      test("get all") {
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
            .get(uri"/companies")
            .send(backendStub)
        } yield response.body
        // inspect resposne
        program.assert { respBody =>
          respBody.toOption.flatMap(_.fromJson[List[Company]].toOption)
          .contains(List(company))
        }
      },
      test("get by id") {
        val program = for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/companies/1")
            .send(backendStub)
        } yield response.body
        // inspect resposne
        program.assert { respBody =>
          val c = respBody.toOption.flatMap(_.fromJson[Company].toOption)
          c.contains(company)
        }
      }
    ).provide(ZLayer.succeed(serviceStub))