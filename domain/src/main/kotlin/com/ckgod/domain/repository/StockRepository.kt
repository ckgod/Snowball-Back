package com.ckgod.domain.repository

import com.ckgod.domain.model.MarketPrice

interface StockRepository {
    suspend fun getStockPrice(userId: String, exchange: String, stockCode: String): MarketPrice?
}
