package com.ckgod.domain.repository

import com.ckgod.domain.model.MarketPrice
import com.ckgod.domain.model.OrderRequest

interface StockRepository {
    suspend fun getCurrentPrice(stockCode: String): MarketPrice?

    suspend fun postOrder(buyOrders: List<OrderRequest> = emptyList(), sellOrders: List<OrderRequest> = emptyList())
}
