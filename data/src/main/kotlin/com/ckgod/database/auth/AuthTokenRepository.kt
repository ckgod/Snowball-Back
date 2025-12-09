package com.ckgod.database.auth

import com.ckgod.domain.auth.AuthToken
import com.ckgod.domain.auth.AuthTokenRepository
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.*
import java.time.LocalDateTime

class AuthTokenRepositoryImpl : AuthTokenRepository {

    override fun getToken(key: String): AuthToken? = transaction {
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

    override fun saveToken(key: String, accessToken: String, expireAt: LocalDateTime): Unit = transaction {
        AuthTokens.deleteWhere { AuthTokens.key eq key }

        AuthTokens.insert {
            it[AuthTokens.key] = key
            it[AuthTokens.accessToken] = accessToken
            it[AuthTokens.expireAt] = expireAt
        }
        Unit
    }

    override fun deleteToken(key: String): Unit = transaction {
        AuthTokens.deleteWhere { AuthTokens.key eq key }
        Unit
    }

    override fun deleteAllTokens(): Unit = transaction {
        AuthTokens.deleteAll()
        Unit
    }
}
