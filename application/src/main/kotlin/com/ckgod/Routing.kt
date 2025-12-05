package com.ckgod

import com.ckgod.service.StockService
import com.ckgod.endpoint.stock.stockRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(stockService: StockService) {
    routing {
        stockRoutes(stockService)
    }
}
