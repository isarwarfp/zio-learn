package com.example.reviewboard.http.domain.data

import com.example.reviewboard.http.domain.data.Company.mkSlug
import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

final case class Company(
  id: Long,
  slug: String,
  name: String, // "My Company Inc" -> companies.scala.com/company/my-company-inc (Will be mapped as)
  url: String,
  location: Option[String] = None,
  country: Option[String] = None,
  industry: Option[String] = None,
  image: Option[String] = None,
  tags: Option[List[String]] = None)

object Company:
  given codec: JsonCodec[Company] = DeriveJsonCodec.gen[Company]

  def mkSlug(name: String): String =
    name.replaceAll(" ", "-")
    .split("-")
    .map(_.toLowerCase)
    .mkString("-")
