package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetCurrentPriceUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.stockRoutes(getCurrentPriceUseCase: GetCurrentPriceUseCase) {
    route("/oversea/nas/price") {
        /**
         * GET /oversea/nas/price?auth=********&code=TQQQ
         */
        get {
            val userId = call.request.queryParameters["auth"]
                ?: return@get call.respondText("id가 누락되었습니다")
            val code = call.request.queryParameters["code"]
                ?: return@get call.respondText("종목 코드가 누락되었습니다.")

            try {
                val marketPrice = getCurrentPriceUseCase(userId, code)
                    ?: throw IllegalArgumentException("존재하지 않는 종목코드 입니다.")

                call.respond(marketPrice)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText("error: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}
