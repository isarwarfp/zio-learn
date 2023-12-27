package com.example

import com.example.TapirDemo.Person
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

object QuillDemo extends ZIOAppDefault:
  val program = for {
    repo <- ZIO.service[PersonRepository]
    _    <- repo.create(Person(1212, "Hussain", 8))
  } yield ()

  override def run: Task[Unit] = program.provide(
    PersonRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase), // Quill Instance
    Quill.DataSource.fromPrefix("myconfig.db") // reads config section from application.conf and spin up a datasource
  )

sealed trait PersonRepository:
  def create(person: Person): Task[Person]
  def update(id: Long, op: Person => Person): Task[Person]
  def delete(id: Long): Task[Person]
  def getById(id: Long): Task[Option[Person]]
  def get: Task[List[Person]]

final case class PersonRepositoryLive(q: Quill.Postgres[SnakeCase]) extends PersonRepository:
  import q.*
  inline given schema: SchemaMeta[Person] = schemaMeta[Person]("persons")
  inline given insMeta: InsertMeta[Person] = insertMeta[Person](_.id) // These columns will be excluded during insert
  inline given updMeta: UpdateMeta[Person] = updateMeta[Person](_.id)

  def create(person: Person): Task[Person] = run {
    query[Person].insertValue(lift(person)).returning(p => p)
  }

  def update(id: Long, op: Person => Person): Task[Person] = for {
    current <- getById(id).someOrFail(new RuntimeException("Could not update, id is not found."))
    updated <- run {
      query[Person]
        .filter(_.id == lift(id))
        .updateValue(lift(current))
        .returning(p => p)
    }
  } yield updated

  def delete(id: Long): Task[Person] = run {
    query[Person].filter(_.id == lift(id)).delete.returning(p => p)
  }

  def getById(id: Long): Task[Option[Person]] = run {
    query[Person].filter(_.id == lift(id))
  }.map(_.headOption)

  def get: Task[List[Person]] = run(query[Person])

object PersonRepositoryLive:
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase]].map(q => PersonRepositoryLive(q))
  }
