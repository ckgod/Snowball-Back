package com.ckgod.domain.model

import kotlinx.serialization.Serializable

/**
 * 무한매수법 전략 설정
 */
@Serializable
data class TradingStrategy(
    val ticker: String,              // 종목 코드 (TQQQ, SOXL)
    val exchange: String, // 거래소
    val divisions: Int = 20,         // 분할 수 (20분할, 40분할 등)
    val basePercent: Double,         // 기준 퍼센트 (TQQQ: 15%, SOXL: 20%)
    val targetPercent: Double,       // 목표 퍼센트 (지정가 매도용)
    val initialCapital: Double,      // 초기 원금 (USD)
) {
    /**
     * T값에 따른 별% 계산
     * TQQQ: 별% = 15 - 1.5T
     * SOXL: 별% = 20 - 2T
     */
    fun calculateStarPercent(tValue: Double): Double {
        val decreasePerT = basePercent / (divisions / 2.0) // 전반전 종료 시점(T=10)에서 0이 되도록
        return basePercent - (decreasePerT * tValue)
    }

    /**
     * 1회 매수금 계산
     */
    fun calculateOneTimeBuyAmount(currentProfit: Double = 0.0): Double {
        val baseAmount = initialCapital / divisions
        // 수익금을 (분할수 × 2)로 나눔 = 40분할 (20분할 기준)
        // 전체 사이클(20회)에 걸쳐 수익의 절반만 반영됨
        // 예: $200 수익 → $200/40 = $5 추가 → 20회 반복 시 $100 반영
        val profitAddition = if (currentProfit > 0)
            currentProfit / (divisions * 2.0)
        else
            0.0
        return baseAmount + profitAddition
    }

    companion object {
        fun forTQQQ(initialCapital: Double, divisions: Int = 20) = TradingStrategy(
            ticker = "TQQQ",
            divisions = divisions,
            basePercent = 15.0,
            targetPercent = 15.0,
            initialCapital = initialCapital,
            exchange = "NASD"
        )

        fun forSOXL(initialCapital: Double, divisions: Int = 20) = TradingStrategy(
            ticker = "SOXL",
            divisions = divisions,
            basePercent = 20.0,
            targetPercent = 20.0,
            initialCapital = initialCapital,
            exchange = "AMEX"
        )
    }
}
