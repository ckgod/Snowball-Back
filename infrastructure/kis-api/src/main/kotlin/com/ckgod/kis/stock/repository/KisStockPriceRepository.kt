package com.ckgod.kis.stock.repository

import com.ckgod.domain.model.StockPrice
import com.ckgod.domain.repository.StockPriceRepository
import com.ckgod.kis.stock.api.KisStockApi

class KisStockPriceRepository(
    private val kisStockApi: KisStockApi
) : StockPriceRepository {
    override suspend fun getStockPrice(stockCode: String): StockPrice {
        val kisData = kisStockApi.getStockPrice(stockCode)

        return StockPrice(
            code = stockCode,
            currentPrice = kisData.stckPrpr,
            changeRate = kisData.prdyCtrt,
            volume = kisData.acmlVol
        )
    }
}
