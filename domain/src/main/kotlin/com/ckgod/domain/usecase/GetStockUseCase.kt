package com.ckgod.domain.usecase

import com.ckgod.domain.stock.Stock
import com.ckgod.domain.stock.StockRepository

class GetStockUseCase(
    private val realRepository: StockRepository,
    private val mockRepository: StockRepository
) {
    suspend operator fun invoke(stockCode: String, isRealMode: Boolean): Stock? {
        val repository = if (isRealMode) realRepository else mockRepository
        val currentPrice = repository.getStockPrice(stockCode)
        repository.updateStock(currentPrice)

        return repository.getStock(stockCode)
    }
}
