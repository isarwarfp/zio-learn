package com.example.reviewboard.http.repositories

import zio.*
import zio.test.*
import com.example.reviewboard.http.domain.data.Review
import java.time.Instant
import com.example.reviewboard.syntax.* 

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript: String = "sql/reviews.sql"

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
  override def spec: Spec[TestEnvironment & Scope, Any] = 
    suite("ReviewRepositorySpec")(
      test("create review") {
        val program = for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        } yield review
        program.assert { r =>
          r.management == goodReview.management &&
          r.culture == goodReview.culture &&
          r.salary == goodReview.salary &&
          r.benefits == goodReview.benefits &&
          r.wouldRecommend == goodReview.wouldRecommend &&
          r.review == goodReview.review
        }
      },
      test("get review by (id, companyId, userId)") {
        for {
          repo        <- ZIO.service[ReviewRepository]
          created     <- repo.create(goodReview)
          byId        <- repo.getById(1L)
          byCompanyId <- repo.getByCompanyId(1L)
          byUserId    <- repo.getByUserId(1L)
        } yield assertTrue (
            byId.contains(created) &&
            byCompanyId.contains(created) &&
            byUserId.contains(created)
        )
      },
      test("edit review") {
        for {
          repo     <- ZIO.service[ReviewRepository]
          created  <- repo.create(goodReview)
          updated  <- repo.update(1L, _.copy(review = "updated review"))
        } yield assertTrue (
            created.id == updated.id &&
            updated.review == "updated review" &&
            updated.culture == created.culture &&
            created.updated != updated.updated
        )
      },
      test("delete review") {
        for {
          repo     <- ZIO.service[ReviewRepository]
          created  <- repo.create(goodReview)
          deleted  <- repo.delete(1L)
          fetched  <- repo.getById(1L)
        } yield assertTrue ( fetched.isEmpty )
      }
    ).provide(
      ReviewRepositoryLive.layer,
      dsLayer,
      Repository.quillLayer,
      Scope.default
    )