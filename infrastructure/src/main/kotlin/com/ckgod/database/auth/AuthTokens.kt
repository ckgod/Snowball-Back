package com.ckgod.database.auth

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object AuthTokens : Table("auth_tokens") {
    val key = varchar("key", 50)
    val accessToken = varchar("access_token", 1024)
    val expireAt = datetime("expire_at")

    override val primaryKey: PrimaryKey = PrimaryKey(key)
}