package com.example.reviewboard.http.gen

import zio.*
import zio.test.*
import com.example.reviewboard.http.domain.data.Company

object CompanyGen:
  def genId: Gen[Any, Long] = Gen.long(1L, Long.MaxValue)
  def genSlug: Gen[Any, String] = Gen.alphaNumericString
  def genName: Gen[Any, String] = Gen.alphaNumericString
  def genUrl: Gen[Any, String] = Gen.alphaNumericString
  def genLocation: Gen[Any, Option[String]] = Gen.option(Gen.alphaNumericString)
  def genCountry: Gen[Any, Option[String]] = Gen.option(Gen.alphaNumericString)
  def genIndustry: Gen[Any, Option[String]] = Gen.option(Gen.alphaNumericString)
  def genImage: Gen[Any, Option[String]] = Gen.option(Gen.alphaNumericString)
  def genTags: Gen[Any, Option[List[String]]] = Gen.option(Gen.listOf(Gen.alphaNumericString))

  def genCompany: Gen[Any, Company] = for {
    id <- genId
    slug <- genSlug
    name <- genName
    url <- genUrl
    location <- genLocation
    country <- genCountry
    industry <- genIndustry
    image <- genImage
    tags <- genTags
  } yield Company(id, slug, name, url)
