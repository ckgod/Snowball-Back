package com.ckgod.database.auth

import java.time.LocalDateTime

data class AuthToken(
    val accessToken: String,
    val expireAt: LocalDateTime
)
