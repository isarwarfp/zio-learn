package com.example.reviewboard.http.repositories

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase

object Repository:
  def quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  def dataSourceLayer = Quill.DataSource.fromPrefix("mydbconf.db")

  val repoLayer = dataSourceLayer >>> quillLayer
