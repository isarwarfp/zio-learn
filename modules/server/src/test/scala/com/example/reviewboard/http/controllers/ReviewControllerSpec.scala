package com.example.reviewboard.http.controllers

import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.test.*
import sttp.client3.*
import zio.json.*
import sttp.tapir.generic.auto.*
import com.example.reviewboard.http.domain.data.Review
import com.example.reviewboard.http.services.ReviewService
import com.example.reviewboard.http.requests.CreateReviewRequest
import com.example.reviewboard.http.controllers.ReviewController
import java.time.Instant

import sttp.tapir.server.ServerEndpoint
import com.example.reviewboard.syntax.*

object ReviewControllerSpec extends ZIOSpecDefault:
  private given zioME: MonadError[Task] = new RIOMonadError[Any]
  private val review = Review(
    id = 1L,
    companyId = 1L,
    userId = 1L,
    management = 5, 
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 5,
    review = "Good",
    created = Instant.now(),
    updated = Instant.now()
  )

  private val createReview = CreateReviewRequest(
    id =  review.id,  
    companyId = review.companyId,  
    management = review.management,  
    culture = review.culture,  
    salary = review.salary,  
    benefits = review.benefits,  
    wouldRecommend = review.wouldRecommend,  
    review =  review.review  
  )
  private val reviewStub = new ReviewService {
    override def create(req: CreateReviewRequest, uId: Long): Task[Review] = ZIO.succeed(review)
    override def getById(id: Long): Task[Option[Review]] = 
      ZIO.succeed(if(id == 1) (Some(review)) else None)
    override def getByCompanyId(id: Long): Task[List[Review]] = 
      ZIO.succeed(if(id == 1) (List(review)) else List.empty)
    override def getByUserId(id: Long): Task[List[Review]] = 
      ZIO.succeed(if(id == 1) (List(review)) else List.empty)
  }

  private def backendStubZIO(endpointFun: ReviewController => ServerEndpoint[Any, Task]) = for {
    controller <- ReviewController.mkZIO
    backendStub <- ZIO.succeed(TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
      .whenServerEndpointRunLogic(endpointFun(controller)).backend())
  } yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewControllerSpec")(
      test("post review") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response    <- basicRequest
            .post(uri"reviews")
            .body(createReview.toJson)
            .send(backendStub)
        } yield response.body
        program.assert { respBody =>
          respBody.toOption.flatMap(_.fromJson[Review].toOption)
          .contains(review)
        }
      },
      test("get by id") {
        val program = for {
          backendStub <- backendStubZIO(_.getById)
          response    <- basicRequest
            .get(uri"/reviews/1")
            .send(backendStub)
        } yield response.body
        program.assert { respBody =>
          val r = respBody.toOption.flatMap(_.fromJson[Review].toOption)
          r.contains(review)
        }
      }
    ).provide(ZLayer.succeed(reviewStub))
