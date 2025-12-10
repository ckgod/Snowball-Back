package com.ckgod.kis.stock.api

import com.ckgod.kis.KisApiClient
import com.ckgod.kis.spec.KisApiSpec
import com.ckgod.kis.stock.response.KisStockPriceResponse

class KisStockApi(private val apiClient: KisApiClient) {

    suspend fun getStockPrice(
        stockCode: String,
        marketDivCode: String = "J"
    ): KisStockPriceResponse {
        val spec = KisApiSpec.InquirePrice
        val queryParams = spec.buildQuery(stockCode, marketDivCode)

        val response = apiClient.request(
            spec = spec,
            queryParams = queryParams
        )

        return KisStockPriceResponse.from(response)
    }
}
