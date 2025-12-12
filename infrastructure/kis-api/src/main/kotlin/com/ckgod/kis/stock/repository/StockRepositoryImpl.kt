package com.ckgod.kis.stock.repository

import com.ckgod.domain.model.MarketPrice
import com.ckgod.domain.repository.StockRepository
import com.ckgod.kis.stock.api.KisApiService

class StockRepositoryImpl(private val kisApiService: KisApiService) : StockRepository {

    override suspend fun getStockPrice(userId: String, exchange: String, stockCode: String): MarketPrice? {
        val kisData = kisApiService.getMarketCurrentPrice(
            userId = userId,
            exchange = exchange,
            stockCode= stockCode
        )

        return kisData.output?.toDomain()
    }
}