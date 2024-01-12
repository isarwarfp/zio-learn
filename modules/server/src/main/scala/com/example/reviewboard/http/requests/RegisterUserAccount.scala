package com.example.reviewboard.http.requests

import zio.json.JsonCodec

final case class RegisterUserAccount(
    email: String,
    password: String
) derives JsonCodec
