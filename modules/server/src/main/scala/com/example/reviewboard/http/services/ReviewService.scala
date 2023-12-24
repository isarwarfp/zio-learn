package com.example.reviewboard.http.services

import zio.*
import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.repositories.ReviewRepository
import com.example.reviewboard.http.requests.CreateReviewRequest
import java.time.Instant

trait ReviewService:
  def create(request: CreateReviewRequest, uId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService:
  override def create(request: CreateReviewRequest, uId: Long): Task[Review] = 
    repo.create(
      Review(
        id = -1L,
        companyId = request.companyId,
        userId = uId,
        management = request.management,
        culture = request.culture,
        salary = request.salary,
        benefits = request.benefits,
        wouldRecommend = request.wouldRecommend,
        review = request.review,
        created = Instant.now(),
        updated = Instant.now()
      )
    )
  override def getById(id: Long): Task[Option[Review]] = 
    repo.getById(id)
  override def getByCompanyId(id: Long): Task[List[Review]] = 
    repo.getByCompanyId(id)
  override def getByUserId(id: Long): Task[List[Review]] = 
    repo.getByUserId(id)

object ReviewServiceLive:
  val layer = ZLayer { ZIO.service[ReviewRepository].map(repo => new ReviewServiceLive(repo)) }
