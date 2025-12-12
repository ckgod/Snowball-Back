package com.ckgod.kis.stock.repository

import com.ckgod.database.stocks.KospiStocks
import com.ckgod.domain.price.MarketPrice
import com.ckgod.domain.price.StockPrice
import com.ckgod.domain.stock.Stock
import com.ckgod.domain.stock.StockRepository
import com.ckgod.kis.stock.api.KisStockApi
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class StockRepositoryImpl(private val kisStockApi: KisStockApi) : StockRepository {
    override fun saveAll(stocks: List<Stock>) = transaction {
        stocks.forEach { stock ->
            KospiStocks.upsert {
                it[KospiStocks.shortCode] = stock.shortCode
                it[KospiStocks.standardCode] = stock.standardCode
                it[KospiStocks.name] = stock.name
                it[KospiStocks.groupCode] = stock.groupCode
                it[KospiStocks.marketCapScale] = stock.marketCapScale
                it[KospiStocks.sectorLarge] = stock.sectorLarge
                it[KospiStocks.basePrice] = stock.basePrice
                it[KospiStocks.isTradingHalted] = stock.isTradingHalted
                it[KospiStocks.isManaged] = stock.isManaged
                it[KospiStocks.marketWarning] = stock.marketWarning
                it[KospiStocks.currentPrice] = stock.currentPrice
                it[KospiStocks.changeRate] = stock.changeRate
                it[KospiStocks.accumulatedVolume] = stock.accumulatedVolume
                it[KospiStocks.changeAmount] = stock.changeAmount
                it[KospiStocks.changeState] = stock.changeState
            }
        }
    }

    override suspend fun getStock(stockCode: String): Stock? = transaction {
        KospiStocks.selectAll()
            .where { KospiStocks.shortCode eq stockCode }
            .firstOrNull()
            ?.let {
                Stock(
                    shortCode = it[KospiStocks.shortCode],
                    standardCode = it[KospiStocks.standardCode],
                    name = it[KospiStocks.name],
                    groupCode = it[KospiStocks.groupCode],
                    marketCapScale = it[KospiStocks.marketCapScale],
                    sectorLarge = it[KospiStocks.sectorLarge],
                    basePrice = it[KospiStocks.basePrice],
                    isTradingHalted = it[KospiStocks.isTradingHalted],
                    isManaged = it[KospiStocks.isManaged],
                    marketWarning = it[KospiStocks.marketWarning],
                    currentPrice = it[KospiStocks.currentPrice],
                    changeRate = it[KospiStocks.changeRate],
                    accumulatedVolume = it[KospiStocks.accumulatedVolume],
                    changeAmount = it[KospiStocks.changeAmount],
                    changeState = it[KospiStocks.changeState]
                )
            }
    }

    override fun deleteAll() = transaction {
        KospiStocks.deleteAll()
    }

    override fun isEmpty(): Boolean = transaction {
        KospiStocks.selectAll().count() == 0L
    }

    override suspend fun getStockPrice(userId: String, stockCode: String): MarketPrice? {
        val kisData = kisStockApi.getMarketCurrentPrice(userId, stockCode)

        return kisData.output?.toDomain()
    }

    override suspend fun updateStock(stockPrice: StockPrice): Unit = transaction {
        KospiStocks.update({ KospiStocks.shortCode eq stockPrice.code }) {
            it[currentPrice] = stockPrice.currentPrice.toLongOrNull()
            it[changeRate] = stockPrice.changeRate
            it[accumulatedVolume] = stockPrice.accumulatedVolume
            it[changeAmount] = stockPrice.changeAmount
            it[changeState] = stockPrice.changeState
        }
        Unit
    }
}