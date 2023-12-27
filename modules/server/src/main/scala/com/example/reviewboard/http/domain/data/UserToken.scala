package com.example.reviewboard.http.domain.data

final case class UserToken(email: String, token: String, expires: Long)
