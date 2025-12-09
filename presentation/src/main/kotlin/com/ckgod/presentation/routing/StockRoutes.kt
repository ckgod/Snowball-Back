package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetStockPriceUseCase
import com.ckgod.presentation.dto.StockPriceResponse
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.stockRoutes(getStockPriceUseCase: GetStockPriceUseCase) {
    route("/api/stock") {
        /**
         * GET /api/stock/price?code=005930&mode=REAL
         * 주식 현재가 조회
         */
        get("/price") {
            val code = call.request.queryParameters["code"]
                ?: return@get call.respondText("종목코드(code)가 누락되었습니다")
            val mode = call.request.queryParameters["mode"] ?: "MOCK"
            val isRealMode = mode.uppercase() == "REAL"

            try {
                val stockPrice = getStockPriceUseCase(code, isRealMode)
                val response = StockPriceResponse(
                    code = stockPrice.code,
                    name = "종목명(추후구현)",
                    currentPrice = stockPrice.currentPrice,
                    changeRate = stockPrice.changeRate,
                    volume = stockPrice.volume
                )
                call.respond(response)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText("error: ${e.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
            }
        }
    }
}
