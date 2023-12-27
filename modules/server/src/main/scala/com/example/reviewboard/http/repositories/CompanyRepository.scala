package com.example.reviewboard.http.repositories
import zio.*
import com.example.reviewboard.http.domain.data.Company
import io.getquill.*
import io.getquill.jdbczio.Quill

trait CompanyRepository:
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def get: Task[List[Company]]

class CompanyRepositoryLive private (q: Quill.Postgres[SnakeCase]) extends CompanyRepository:
  import q.*

  inline given schema: SchemaMeta[Company] = schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given updMeta: UpdateMeta[Company] = updateMeta[Company](_.id)

  def create(company: Company): Task[Company] = run {
    query[Company]
    .insertValue(lift(company))
    .returning(c => c)
  }
  def update(id: Long, op: Company => Company): Task[Company] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Could not update, id: $id is not found."))
    updated <- run {
      query[Company]
      .filter(_.id == lift(id))
      .updateValue(lift(op(current)))
      .returning(c => c)
    }
  } yield updated

  def delete(id: Long): Task[Company] = run {
    query[Company]
    .filter(_.id == lift(id))
    .delete
    .returning(c => c)
  }
  def getById(id: Long): Task[Option[Company]] = run {
    query[Company].filter(_.id == lift(id))
  }.map(_.headOption)
  def getBySlug(slug: String): Task[Option[Company]] = run {
    query[Company].filter(_.slug == lift(slug))
  }.map(_.headOption)
  def get: Task[List[Company]] = run { query[Company] }

object CompanyRepositoryLive:
  val layer = ZLayer { 
    ZIO.service[Quill.Postgres[SnakeCase.type]].map(q => CompanyRepositoryLive(q))
  }

object CompanyRespositoryDemo extends ZIOAppDefault:
  val program = for {
    service <- ZIO.service[CompanyRepository]
    _ <- service.create(Company(1, "my-company", "My Company", "www.go.com"))
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = program.provide(
    CompanyRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("myconfig.db")
  )