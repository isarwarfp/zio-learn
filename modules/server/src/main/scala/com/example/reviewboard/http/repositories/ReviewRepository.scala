package com.example.reviewboard.http.repositories

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import com.example.reviewboard.http.domain.data.*

trait ReviewRepository:
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(id: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]

class ReviewRepositoryLive private (q: Quill.Postgres[SnakeCase]) extends ReviewRepository:

  import q.*
  inline given schema: SchemaMeta[Review] = schemaMeta[Review]("reviews")
  inline given insMeta: InsertMeta[Review] = insertMeta[Review](_.id, _.created, _.updated)
  inline given updMeta: UpdateMeta[Review] = updateMeta[Review](_.id, _.companyId, _.userId, _.created)
  def create(review: Review): Task[Review] = 
    run(query[Review].insertValue(lift(review)).returning(r => r))
  def getById(id: Long): Task[Option[Review]] = 
    run(query[Review].filter(_.id == lift(id))).map(_.headOption)
  def getByCompanyId(id: Long): Task[List[Review]] = 
    run(query[Review].filter(_.companyId == lift(id)))
  def getByUserId(id: Long): Task[List[Review]] = 
    run(query[Review].filter(_.userId == lift(id)))
  def update(id: Long, op: Review => Review): Task[Review] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Could not update, id: $id is not found."))
    updated <- run {
      query[Review]
      .filter(_.id == lift(id))
      .updateValue(lift(op(current)))
      .returning(r => r)
    }
  } yield updated
  def delete(id: Long): Task[Review] = run {
    query[Review]
    .filter(_.id == lift(id))
    .delete
    .returning(r => r)
  }

object ReviewRepositoryLive:
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(q => ReviewRepositoryLive(q))
  }
