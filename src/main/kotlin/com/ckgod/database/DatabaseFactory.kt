package com.ckgod.database

import com.ckgod.database.auth.AuthTokens
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

object DatabaseFactory {
    private var initialized = false

    fun init() {
        if (initialized) {
            println("Database already initialized, skipping...")
            return
        }

        val dbFile = File("./data/stock_db")
        val driverClassName = "org.h2.Driver"
        val jdbcUrl = "jdbc:h2:file:${dbFile.absolutePath};DB_CLOSE_DELAY=-1;"

        Database.connect(jdbcUrl, driverClassName)

        transaction {
            SchemaUtils.create(AuthTokens)
        }

        initialized = true
        println("Database initialized successfully")
    }
}