package com.example.reviewboard.http.services

import zio.*
import zio.test.*
import java.time.Instant
import com.example.reviewboard.http.requests.CreateReviewRequest
import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.syntax.*
import com.example.reviewboard.http.repositories.ReviewRepository
import com.example.reviewboard.http.services.ReviewService

object ReviewServiceSpec extends ZIOSpecDefault:
  val service = ZIO.serviceWithZIO[ReviewService]
  val goodReview = Review(
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
  
  val stubRepoLayer = ZLayer.succeed {
    new ReviewRepository {
      override def create(review: Review): Task[Review] = ZIO.succeed(goodReview)
      override def getById(id: Long): Task[Option[Review]] = 
        ZIO.succeed(if(id == 1) Some(goodReview) else None)
      override def getByUserId(id: Long): Task[List[Review]] = 
        ZIO.succeed(if(id == 1) List(goodReview) else List.empty)
      override def getByCompanyId(id: Long): Task[List[Review]] = 
        ZIO.succeed(if(id == 1) List(goodReview) else List.empty)
      override def delete(id: Long): Task[Review] = 
        getById(id).someOrFail(new RuntimeException(s"Id $id not found."))
      override def update(id: Long, op: Review => Review): Task[Review] = 
        getById(id).someOrFail(new RuntimeException(s"Id $id not found."))
    }
  }
  override def spec: Spec[TestEnvironment & Scope, Any] = 
    val reviewZIO = service(_.create(CreateReviewRequest(
      id =  goodReview.id,  
      companyId = goodReview.companyId,  
      management = goodReview.management,  
      culture = goodReview.culture,  
      salary = goodReview.salary,  
      benefits = goodReview.benefits,  
      wouldRecommend = goodReview.wouldRecommend,  
      review =  goodReview.review  
    ), goodReview.userId))
    suite("ReviewServiceSpec")(
      test("create") {
        for {
          created <- reviewZIO 
        } yield assertTrue (
          created.id == goodReview.id &&
          created.companyId == goodReview.companyId &&
          created.management == goodReview.management &&
          created.culture == goodReview.culture &&
          created.salary == goodReview.salary &&
          created.benefits == goodReview.benefits &&
          created.wouldRecommend == goodReview.wouldRecommend &&
          created.review == goodReview.review
        )
      }
    ).provide(ReviewServiceLive.layer, stubRepoLayer)