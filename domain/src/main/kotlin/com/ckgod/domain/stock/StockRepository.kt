package com.ckgod.domain.stock

import com.ckgod.domain.price.MarketPrice
import com.ckgod.domain.price.StockPrice

interface StockRepository {
    fun saveAll(stocks: List<Stock>)
    fun deleteAll(): Int
    fun isEmpty(): Boolean
    suspend fun getStockPrice(userId: String, stockCode: String): MarketPrice?
    suspend fun getStock(stockCode: String): Stock?
    suspend fun updateStock(stockPrice: StockPrice)
}
