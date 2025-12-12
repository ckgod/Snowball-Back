package com.ckgod.domain.usecase

import com.ckgod.domain.model.MarketPrice
import com.ckgod.domain.repository.StockRepository

class GetCurrentPriceUseCase(
    private val repository: StockRepository,
) {
    suspend operator fun invoke(userId: String, exchange: String, stockCode: String): MarketPrice? {
        return repository.getStockPrice(
            userId = userId,
            stockCode = stockCode,
            exchange = exchange
        )
    }
}
