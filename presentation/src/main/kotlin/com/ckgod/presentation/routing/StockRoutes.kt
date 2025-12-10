package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetStockUseCase
import com.ckgod.presentation.dto.StockResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.stockRoutes(getStockUseCase: GetStockUseCase) {
    route("/api/stock") {
        /**
         * GET /api/stock?code=005930&mode=REAL
         * 주식 현재가 조회
         */
        get {
            val code = call.request.queryParameters["code"]
                ?: return@get call.respondText("종목코드(code)가 누락되었습니다")
            val mode = call.request.queryParameters["mode"] ?: "MOCK"
            val isRealMode = mode.uppercase() == "REAL"

            try {
                val stock = getStockUseCase(code, isRealMode)
                    ?: throw IllegalArgumentException("존재하지 않는 종목코드 입니다.")
                val response = StockResponse.from(stock)
                call.respond(response)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText("error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}
