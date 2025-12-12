package com.ckgod.kis.stock.api

import com.ckgod.kis.KisApiClient
import com.ckgod.kis.spec.KisApiSpec
import com.ckgod.kis.stock.response.KisPriceResponse

class KisStockApi(private val apiClient: KisApiClient) {

    suspend fun getMarketCurrentPrice(
        userId: String,
        stockCode: String,
    ): KisPriceResponse {
        val spec = KisApiSpec.QuotationPriceDetail
        val queryParams = spec.buildQuery(
            userId = userId,
            stockCode = stockCode
        )

        return apiClient.request(
            spec = spec,
            queryParams = queryParams
        )
    }
}
