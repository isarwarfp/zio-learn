package com.example.reviewboard.http.requests

import zio.json.*

final case class LoginRequest(
    email: String,
    password: String
) derives JsonCodec
