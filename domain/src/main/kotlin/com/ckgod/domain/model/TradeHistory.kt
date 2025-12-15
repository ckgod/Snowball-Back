package com.ckgod.domain.model

/**
 * 거래 이력 데이터
 */
data class TradeHistory(
    val id: String,
    val strategyStateId: String,
    val userId: String,
    val ticker: String,
    val orderSide: String, // BUY, SELL
    val price: Double,
    val quantity: Double,
    val amount: Double,
    val tValue: Double,
    val starPercent: Double,
    val phase: String,
    val profit: Double = 0.0,
    val executedAt: String
)
