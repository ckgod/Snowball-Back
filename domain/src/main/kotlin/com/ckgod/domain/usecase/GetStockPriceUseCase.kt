package com.ckgod.domain.usecase

import com.ckgod.domain.model.StockPrice
import com.ckgod.domain.repository.StockPriceRepository

class GetStockPriceUseCase(
    private val realRepository: StockPriceRepository,
    private val mockRepository: StockPriceRepository
) {
    suspend operator fun invoke(stockCode: String, isRealMode: Boolean): StockPrice {
        val repository = if (isRealMode) realRepository else mockRepository
        return repository.getStockPrice(stockCode)
    }
}
