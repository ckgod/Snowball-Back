package com.ckgod.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountStatus(
    val totalPurchaseAmount: String, // 총 매수 금액 (USD)
    val totalEvaluationAmount: String, // 총 평가 금액 (USD)
    val totalProfitOrLoss: String, // 총 손익 (USD)
    val totalProfitRate: String, // 총 수익률 (%)
    val holdings: List<StockHolding> // 보유 종목 리스트
)

@Serializable
data class StockHolding(
    val ticker: String,            // 티커 (TQQQ, SOXL)
    val name: String,              // 종목명
    val quantity: String,          // 보유 수량 (실수형, 미주는 소수점 가능)
    val avgPrice: String,          // 내 평단가
    val investedAmount: String,    // 매수 누적액
    val currentPrice: String,      // 현재가
    val profitRate: String         // 수익률
) {
    fun calculateTValue(oneTimeBuyAmount: Double): Double {
        if (oneTimeBuyAmount == 0.0) return 0.0
        return investedAmount.toDouble() / oneTimeBuyAmount
    }
}