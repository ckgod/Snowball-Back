package com.ckgod.domain.price

data class StockPrice(
    val code: String,
    val currentPrice: String,
    val changeRate: String,
    val accumulatedVolume: Long,
    val changeAmount: Long,
    val changeState: String
)
