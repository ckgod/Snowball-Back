package com.ckgod.domain.usecase

import com.ckgod.domain.price.MarketPrice
import com.ckgod.domain.stock.StockRepository

class GetCurrentPriceUseCase(
    private val repository: StockRepository,
) {
    suspend operator fun invoke(userId: String, stockCode: String): MarketPrice? {
        return repository.getStockPrice(userId, stockCode)
    }
}
