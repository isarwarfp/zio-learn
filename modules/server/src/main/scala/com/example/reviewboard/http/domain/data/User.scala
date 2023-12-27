package com.example.reviewboard.http.domain.data

final case class User(
  id: Long,
  email: String,
  hashedPwd: String
) {
  def toUserId: UserId = UserId(id, email)
}

final case class UserId(
  id: Long,
  email: String
)
