package com.ckgod.kis.stock.repository

import com.ckgod.domain.model.MarketPrice
import com.ckgod.domain.repository.StockRepository
import com.ckgod.kis.stock.api.KisStockApi

class StockRepositoryImpl(private val kisStockApi: KisStockApi) : StockRepository {

    override suspend fun getStockPrice(userId: String, stockCode: String): MarketPrice? {
        val kisData = kisStockApi.getMarketCurrentPrice(userId, stockCode)

        return kisData.output?.toDomain()
    }
}