package com.example.reviewboard.http.domain.data

final case class User(
  id: Long,
  email: String,
  hashedPwd: String
)
