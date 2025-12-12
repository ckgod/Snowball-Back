package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetCurrentPriceUseCase
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(getCurrentPriceUseCase: GetCurrentPriceUseCase) {
    routing {
        stockRoutes(getCurrentPriceUseCase)
    }
}
