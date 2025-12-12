package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetAccountStatusUseCase
import com.ckgod.domain.usecase.GetCurrentPriceUseCase
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userId: String,
    getCurrentPriceUseCase: GetCurrentPriceUseCase,
    getAccountStatusUseCase: GetAccountStatusUseCase
) {
    routing {
        stockRoutes(userId, getCurrentPriceUseCase)
        accountRoutes(getAccountStatusUseCase)
    }
}
