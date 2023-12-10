package com.example.reviewboard.http.services
import com.example.reviewboard.http.domain.data.Company
import com.example.reviewboard.http.requests.CreateCompanyRequest
import zio.*
import com.example.reviewboard.http.repositories.CompanyRepository

// Business Logic
trait CompanyService:
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll: Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]

object CompanyService:
  val serviceLayer = ZLayer.succeed(new CompanyServiceDummy)

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService:
  override def create(req: CreateCompanyRequest): Task[Company] = repo.create(req.toCompany(-1L))
  override def getAll: Task[List[Company]] = repo.get
  override def getById(id: Long): Task[Option[Company]] = repo.getById(id)
  override def getBySlug(slug: String): Task[Option[Company]] = repo.getBySlug(slug)

class CompanyServiceDummy extends CompanyService:
  var db: Map[Long, Company] = Map.empty
  override def create(req: CreateCompanyRequest): Task[Company] = 
    ZIO.succeed {
      val id      = db.keys.maxOption.getOrElse(0L) + 1
      val company = req.toCompany(id)
      db          = db + (id -> company)
      company
    }
  override def getAll: Task[List[Company]] = ZIO.succeed(db.values.toList)
  override def getById(id: Long): Task[Option[Company]] = ZIO.attempt(db.get(id))
  override def getBySlug(slug: String): Task[Option[Company]] = ZIO.attempt(db.values.find(_.slug == slug))

