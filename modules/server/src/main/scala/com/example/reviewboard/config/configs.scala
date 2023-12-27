package com.example.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.{Tag, ZIO, ZLayer}
import zio.config.typesafe.TypesafeConfig

import zio.config.*
import zio.config.magnolia.*

object configs:
  def mkLayer[C](path: String)(using Descriptor[C], Tag[C]): ZLayer[Any, Throwable, C] =
    TypesafeConfig.fromTypesafeConfig(
      ZIO.attempt(ConfigFactory.load().getConfig(path)),
      descriptor[C]
    )
