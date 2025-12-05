package com.ckgod.domain.auth

import java.time.LocalDateTime

interface AuthTokenRepository {
    fun getToken(key: String): AuthToken?
    fun saveToken(key: String, accessToken: String, expireAt: LocalDateTime)
    fun deleteToken(key: String)
    fun deleteAllTokens()
}
