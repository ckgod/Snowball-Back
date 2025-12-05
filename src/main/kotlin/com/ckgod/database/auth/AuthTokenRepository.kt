package com.ckgod.database.auth

import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*
import java.time.LocalDateTime

data class AuthToken(
    val accessToken: String,
    val expireAt: LocalDateTime
)

object AuthTokenRepository {

    fun getToken(key: String): AuthToken? = transaction {
        AuthTokens.selectAll()
            .where { AuthTokens.key eq key }
            .firstOrNull()
            ?.let {
                AuthToken(
                    accessToken = it[AuthTokens.accessToken],
                    expireAt = it[AuthTokens.expireAt]
                )
            }
    }

    fun saveToken(key: String, accessToken: String, expireAt: LocalDateTime) = transaction {
        AuthTokens.deleteWhere { AuthTokens.key eq key }

        AuthTokens.insert {
            it[AuthTokens.key] = key
            it[AuthTokens.accessToken] = accessToken
            it[AuthTokens.expireAt] = expireAt
        }
    }

    fun deleteToken(key: String) = transaction {
        AuthTokens.deleteWhere { AuthTokens.key eq key }
    }

    fun deleteAllTokens() = transaction {
        AuthTokens.deleteAll()
    }
}
