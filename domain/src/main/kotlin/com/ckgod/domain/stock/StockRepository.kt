package com.ckgod.domain.stock

import com.ckgod.domain.model.StockPrice

interface StockRepository {
    fun saveAll(stocks: List<Stock>)
    fun deleteAll(): Int
    fun isEmpty(): Boolean
    suspend fun getStockPrice(stockCode: String): StockPrice
    suspend fun getStock(stockCode: String): Stock?
    suspend fun updateStock(stockPrice: StockPrice)
}
