package com.example.reviewboard.http.services

import zio.*
import zio.test.*
import com.example.reviewboard.http.requests.CreateCompanyRequest
import com.example.reviewboard.http.repositories.Repository
import com.example.reviewboard.http.domain.data.Company
import com.example.reviewboard.syntax.*
import com.example.reviewboard.http.repositories.CompanyRepositoryLive
import com.example.reviewboard.http.repositories.CompanyRepository

object CompanyServiceSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment & Scope, Any] = 
    val service = ZIO.serviceWithZIO[CompanyService]
    var db: Map[Long, Company] = Map.empty
    val stubRepoLayer = ZLayer.succeed(
      new CompanyRepository {
        override def create(company: Company): Task[Company] = ZIO.succeed {
          val nextId = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = company.copy(id = nextId)
          db = db + (nextId -> newCompany)
          newCompany
        }
        override def update(id: Long, op: Company => Company): Task[Company] = ZIO.attempt {
          val company = db(id)
          db = db + (id -> op(company))
          company
        }
        override def delete(id: Long): Task[Company] = ZIO.attempt {
          val company = db(id)
          db = db - id
          company
        }
        override def getById(id: Long): Task[Option[Company]] = ZIO.succeed(db.get(id))
        override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed(db.values.find(_.slug == slug))
        override def get: Task[List[Company]] = ZIO.succeed(db.values.toList)
      }
    )
    val companyZIO = service(_.create(CreateCompanyRequest("My Company", "www.go.com")))
    suite("CompanyServiceSpec")(
      test("create") {
        companyZIO.assert { company => 
          company.name == "My Company" &&
          company.slug == "my-company" &&
          company.url  == "www.go.com" 
        }
      },
      test("get by id") {
        val program = for {
          c <- companyZIO
          companyOpt <- service(_.getById(c.id))
        } yield (c, companyOpt)
        program.assert {
          case (company, Some(companyOpt)) =>
            company.name == "My Company" &&
            company.slug == "my-company" &&
            company.url  == "www.go.com" &&
            company == companyOpt
          case _ => false
        }
      },
      test("get by slug") {
        val program = for {
          c <- companyZIO
          companyOpt <- service(_.getBySlug(c.slug))
        } yield (c, companyOpt)
        program.assert {
          case (company, Some(companyOpt)) =>
            company.name == "My Company" &&
            company.slug == "my-company" &&
            company.url  == "www.go.com" &&
            company == companyOpt
          case _ => false
        }
      },
      test("get all") {
        val program = for {
          c1 <- companyZIO
          c2 <- service(_.create(CreateCompanyRequest("Your Company", "www.you.com")))
          companies <- service(_.getAll)
        } yield (c1, c2, companies)
        program.assert {
          case (c1, c2, companies) => companies.toSet == Set(c1, c2)
        }
      }
    ).provide(CompanyServiceLive.layer, stubRepoLayer)