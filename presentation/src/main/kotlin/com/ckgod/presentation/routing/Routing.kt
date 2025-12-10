package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetStockUseCase
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(getStockUseCase: GetStockUseCase) {
    routing {
        stockRoutes(getStockUseCase)
    }
}
