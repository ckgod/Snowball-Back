package com.ckgod

import com.ckgod.database.DatabaseFactory
import com.ckgod.database.auth.AuthTokenRepository
import com.ckgod.domain.usecase.GetAccountStatusUseCase
import com.ckgod.domain.usecase.GetCurrentPriceUseCase
import com.ckgod.kis.KisApiClient
import com.ckgod.kis.auth.KisAuthService
import com.ckgod.kis.config.KisConfig
import com.ckgod.kis.config.KisMode
import com.ckgod.kis.stock.api.KisApiService
import com.ckgod.kis.stock.repository.AccountRepositoryImpl
import com.ckgod.kis.stock.repository.StockRepositoryImpl
import com.ckgod.presentation.config.configureAuthPlugin
import com.ckgod.presentation.config.configureRateLimiter
import com.ckgod.presentation.config.configureSerialization
import com.ckgod.presentation.routing.configureRouting
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json

fun main() {
    val server = embeddedServer(Netty, port = 8080, module = Application::module)

    server.start(wait = true)
}

@Suppress("unused")
fun Application.mainModule() {
    module()
}

fun Application.module() {
    // ========== Infrastructure Setup ==========
    DatabaseFactory.init()

    val config = environment.config

    val httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // ========== Security Configuration ==========
    val apiKey = config.property("api.key").getString()
    val maxRequests = config.property("api.rateLimit.maxRequests").getString().toInt()
    val windowSeconds = config.property("api.rateLimit.windowSeconds").getString().toLong()
    val kisUserId = config.property("kis.userId").getString().trim()

    // ========== Configuration ==========
    val realConfig = KisConfig(
        mode = KisMode.REAL,
        baseUrl = config.property("kis.real.baseUrl").getString().trim(),
        appKey = config.property("kis.real.appKey").getString().trim(),
        appSecret = config.property("kis.real.appSecret").getString().trim(),
        accountNo = config.property("kis.real.accountNo").getString().trim(),
        accountCode = config.property("kis.real.accountCode").getString().trim(),
    )

    val mockConfig = KisConfig(
        mode = KisMode.MOCK,
        baseUrl = config.property("kis.mock.baseUrl").getString().trim(),
        appKey = config.property("kis.mock.appKey").getString().trim(),
        appSecret = config.property("kis.mock.appSecret").getString().trim(),
        accountNo = config.property("kis.mock.accountNo").getString().trim(),
        accountCode = config.property("kis.mock.accountCode").getString().trim(),
    )

    // ========== Data Layer (Repositories) ==========
    val authTokenRepository = AuthTokenRepository()

    val realAuthService = KisAuthService(realConfig, httpClient, authTokenRepository)
    val mockAuthService = KisAuthService(mockConfig, httpClient, authTokenRepository)

    val realApiClient = KisApiClient(realConfig, realAuthService, httpClient)
    val mockApiClient = KisApiClient(mockConfig, mockAuthService, httpClient)

    val realApiService = KisApiService(realApiClient)
    val mockApiService = KisApiService(mockApiClient)

    val realStockRepository = StockRepositoryImpl(realApiService)
    val mockStockRepository = StockRepositoryImpl(mockApiService)
    val realAccountRepository = AccountRepositoryImpl(realApiService)
    val mockAccountRepository = AccountRepositoryImpl(mockApiService)

    // ========== Domain Layer (Use Cases) ==========
    val getCurrentPriceUseCase = GetCurrentPriceUseCase(realStockRepository)
    val getAccountStatusUseCase = GetAccountStatusUseCase(realAccountRepository, mockAccountRepository)

    // ========== Presentation Layer Setup ==========
    configureAuthPlugin(apiKey)
    configureRateLimiter(maxRequests, windowSeconds)

    configureSerialization()
    configureRouting(
        userId = kisUserId,
        getCurrentPriceUseCase = getCurrentPriceUseCase,
        getAccountStatusUseCase = getAccountStatusUseCase,
    )
}
