package com.ckgod.kis.stock.api

import com.ckgod.kis.KisApiClient
import com.ckgod.kis.spec.KisApiSpec
import com.ckgod.kis.stock.response.KisBalanceResponse
import com.ckgod.kis.stock.response.KisPriceResponse

class KisApiService(private val apiClient: KisApiClient) {

    suspend fun getMarketCurrentPrice(
        stockCode: String
    ): KisPriceResponse {
        val spec = KisApiSpec.QuotationPriceDetail
        val exchange = when(stockCode) {
            "TQQQ" -> "NAS"
            "SOXL" -> "AMS"
            else -> "NAS"
        }
        val queryParams = spec.buildQuery(
            userId = apiClient.config.userId,
            exchange = exchange,
            stockCode = stockCode
        )

        return apiClient.request(
            spec = spec,
            queryParams = queryParams
        )
    }

    suspend fun getAccountBalance() : KisBalanceResponse {
        val spec = KisApiSpec.InquireBalance
        val queryParams = spec.buildQuery(
            accountNo = apiClient.config.accountNo,
            accountCode = apiClient.config.accountCode
        )

        return apiClient.request(
            spec = spec,
            queryParams = queryParams
        )
    }
}
