package com.example.reviewboard.http.repositories

import zio.*
import zio.test.*
import com.example.reviewboard.syntax.* 
import com.example.reviewboard.http.domain.data.Company
import org.testcontainers.containers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import com.example.reviewboard.http.repositories.Repository.dataSourceLayer
import java.sql.SQLException
import com.example.reviewboard.http.gen.CompanyGen.*
import zio.test.TestAspect.*

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec:
  override val initScript = "sql/companies.sql"
  private val company = Company(1L, "my-company", "My Company", "www.go.com")
  override def spec: Spec[TestEnvironment & Scope, Any] = 
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(company)
        } yield company
        program.assert {
          case Company(_, "my-company", "My Company", "www.go.com", _, _, _, _, _) => true
          case _                                                                   => false
        }
      },
      test("should error on duplicate company creation") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          _ <- repo.create(company)
          err <- repo.create(company).flip
        } yield err
        program.assert(_.isInstanceOf[SQLException])
      },
      test("get by id and slug") {
        val program = for {
          repo  <- ZIO.service[CompanyRepository]
          c     <- repo.create(company)
          c1    <- repo.getById(c.id)
          c2    <- repo.getBySlug(c.slug)
        } yield (c, c1, c2)
        program.assert { 
          case (c, c1, c2) => c1.contains(c) && c2.contains(c)
        }
      },
      test("update a company") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(company)
          updated     <- repo.update(company.id, _.copy(url = "www.you.com"))
          afterUpdate <- repo.getById(company.id)
        } yield (updated, afterUpdate)
        program.assert {
          case (upd, afterUpd) => afterUpd.contains(upd)
        }
      },
      test("delete a company") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(company)
          deleted     <- repo.delete(company.id)
          afterDelete <- repo.getById(company.id)
        } yield afterDelete
        program.assert(_.isEmpty)
      },
      test("get all companies") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          companies <- genCompany.runCollectN(3) // TODO: there is some bug
          created <- ZIO.foreach(companies)(repo.create)
          fetched <- repo.get
        } yield (created, fetched)
        program.assert {
          case (created, fetched) => fetched.nonEmpty && created.toSet == fetched.toSet
        }
      } @@ ignore
    ).provide(
      CompanyRepositoryLive.layer,
      dsLayer,
      Repository.quillLayer,
      Scope.default
    )