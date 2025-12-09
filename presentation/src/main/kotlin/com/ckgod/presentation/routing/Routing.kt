package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetStockPriceUseCase
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(getStockPriceUseCase: GetStockPriceUseCase) {
    routing {
        stockRoutes(getStockPriceUseCase)
    }
}
