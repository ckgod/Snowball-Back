package com.ckgod.kis.stock.api

import com.ckgod.kis.KisApiClient
import com.ckgod.kis.spec.KisApiSpec
import com.ckgod.kis.stock.response.KisBalanceResponse
import com.ckgod.kis.stock.response.KisPriceResponse

class KisApiService(private val apiClient: KisApiClient) {

    suspend fun getMarketCurrentPrice(
        userId: String,
        stockCode: String,
        exchange: String
    ): KisPriceResponse {
        val spec = KisApiSpec.QuotationPriceDetail
        val queryParams = spec.buildQuery(
            userId = userId,
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
