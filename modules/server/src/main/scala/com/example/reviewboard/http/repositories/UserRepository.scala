package com.example.reviewboard.http.repositories

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import com.example.reviewboard.http.domain.data.*

trait UserRepository:
  def create(user: User): Task[User]
  def getById(id: Long): Task[Option[User]]
  def getByEmail(email: String): Task[Option[User]]
  def update(id: Long, op: User => User): Task[User]
  def delete(id: Long): Task[User]

class UserRepositoryLive private (q: Quill.Postgres[SnakeCase]) extends UserRepository:
  import q.*

  inline given schema: SchemaMeta[User] = schemaMeta[User]("users")
  inline given insMeta: InsertMeta[User] = insertMeta[User](_.id)
  inline given updMeta: UpdateMeta[User] = updateMeta[User](_.id)

  override def create(user: User): Task[User] = run {
    query[User]
    .insertValue(lift(user))
    .returning(c => c)
  }
  override def getById(id: Long): Task[Option[User]] = run {
    query[User].filter(_.id == lift(id))
  }.map(_.headOption)

  override def getByEmail(email: String): Task[Option[User]] = run {
    query[User].filter(_.email == lift(email))
  }.map(_.headOption)

  override def update(id: Long, op: User => User): Task[User] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Could not update, id: $id is not found."))
    updated <- run {
      query[User]
      .filter(_.id == lift(id))
      .updateValue(lift(op(current)))
      .returning(c => c)
    }
  } yield updated

  override def delete(id: Long): Task[User] = run {
    query[User]
    .filter(_.id == lift(id))
    .delete
    .returning(c => c)
  }

object UserRepositoryLive:
  val layer = ZLayer { 
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(q => UserRepositoryLive(q))
  }


