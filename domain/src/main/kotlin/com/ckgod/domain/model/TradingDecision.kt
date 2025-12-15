package com.ckgod.domain.model

/**
 * 매매 결정 결과
 */
data class TradingDecision(
    val state: StrategyState,
    val currentPrice: Double,
    val buyOrders: List<Order>,
    val sellOrders: List<Order>,
    val reason: String
) {
    val hasOrders: Boolean
        get() = buyOrders.isNotEmpty() || sellOrders.isNotEmpty()

    val totalBuyAmount: Double
        get() = buyOrders.sumOf { it.amount }

    val totalSellQuantity: Double
        get() = sellOrders.sumOf { it.quantity }
}
