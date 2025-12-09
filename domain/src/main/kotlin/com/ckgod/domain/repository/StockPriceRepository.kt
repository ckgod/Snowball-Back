package com.ckgod.domain.repository

import com.ckgod.domain.model.StockPrice

interface StockPriceRepository {
    suspend fun getStockPrice(stockCode: String): StockPrice
}
