package com.ckgod.presentation.routing

import com.ckgod.domain.usecase.GetAccountStatusUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.accountRoutes(
    getAccountStatusUseCase: GetAccountStatusUseCase,
) {
    route("/account/balance") {
        get {
            try {
                val mode = call.request.queryParameters["mode"] ?: "REAL"

                val accountStatus = getAccountStatusUseCase(
                    mode == "REAL"
                )
                call.respond(accountStatus)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "잘못된 요청입니다")
                )
            } catch (e: Exception) {
                println("Error processing stock price request: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "요청 처리 중 오류가 발생했습니다")
                )
            }
        }
    }
}