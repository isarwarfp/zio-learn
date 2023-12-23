package com.example.reviewboard.http.repositories

import zio.*
import com.example.reviewboard.http.domain.data.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait ReviewRepository:
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]

class ReviewRepositoryLive private (q: Quill.Postgres[SnakeCase]) extends ReviewRepository:
  def create(review: Review): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))
  def getById(id: Long): Task[Option[Review]] = ZIO.fail(new RuntimeException("not implemented"))
  def getByCompanyId(id: Long): Task[List[Review]] = ZIO.fail(new RuntimeException("not implemented"))
  def getByUserId(id: Long): Task[List[Review]] = ZIO.fail(new RuntimeException("not implemented"))
  def update(id: Long, op: Review => Review): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))
  def delete(id: Long): Task[Review] = ZIO.fail(new RuntimeException("not implemented"))

object ReviewRepositoryLive:
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(q => ReviewRepositoryLive(q))
  }
