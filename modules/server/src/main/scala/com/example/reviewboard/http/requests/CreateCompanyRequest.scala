package com.example.reviewboard.http.requests

import com.example.reviewboard.http.domain.data.Company
import com.example.reviewboard.http.domain.data.Company.*
import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

final case class CreateCompanyRequest(
  name: String,
  url: String,
  location: Option[String] = None,
  country: Option[String] = None,
  industry: Option[String] = None,
  image: Option[String] = None,
  tags: Option[List[String]] = None):

  def toCompany(id: Long) =
    Company(id, mkSlug(name), name, url, location, country, industry, image, tags)

object CreateCompanyRequest:
  given codec: JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen[CreateCompanyRequest]
