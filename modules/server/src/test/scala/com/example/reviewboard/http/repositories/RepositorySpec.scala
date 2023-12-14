package com.example.reviewboard.http.repositories

import zio.*
import zio.test.*

import org.testcontainers.containers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import com.example.reviewboard.http.repositories.Repository.dataSourceLayer
import java.sql.SQLException

trait RepositorySpec:
  // Steps for Test Containers
  // 1. Spawn a postgres container on docker just for the test
  private def mkContainer() = {
    val container: PostgreSQLContainer[Nothing] = PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")
    container.start()
    container
  }

  // 2. Create datasource to connect to postgress
  private def mkDataSource(container: PostgreSQLContainer[Nothing]) = {
    val ds = new PGSimpleDataSource()
    ds.setURL(container.getJdbcUrl())
    ds.setUser(container.getUsername())
    ds.setPassword(container.getPassword())
    ds
  }

  // 3. Use datasoruce (ZLayer) to build Quill instance, will be also as ZLayer
  val dsLayer = ZLayer {
    for {
      container  <- ZIO.acquireRelease(ZIO.attempt(mkContainer()))(c => ZIO.attempt(c.stop()).ignoreLogged)
      datasource <- ZIO.attempt(mkDataSource(container))
    } yield datasource
  }
