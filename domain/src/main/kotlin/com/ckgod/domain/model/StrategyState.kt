package com.ckgod.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.math.ceil

/**
 * 무한매수법 전략 실행 상태
 */
@Serializable
data class StrategyState(
    val id: String,                          // 전략 상태 ID
    val userId: String,                      // 사용자 ID
    val strategy: TradingStrategy,           // 전략 설정
    val cycleNumber: Int = 1,                // 사이클 번호 (1부터 시작)
    val accumulatedInvestment: Double = 0.0, // 매수 누적액 (USD)
    val accumulatedQuantity: Double = 0.0,   // 누적 수량
    val averagePrice: Double = 0.0,          // 평단가
    val totalProfit: Double = 0.0,           // 누적 총 수익 (전체 사이클 합계)
    val currentCycleProfit: Double = 0.0,    // 현재 사이클 수익
    val oneTimeBuyAmount: Double,            // 현재 1회 매수금
    val reservedProfit: Double = 0.0,        // 수익금 절반 보유분 (쿼터모드 사용)
    val isActive: Boolean = true,            // 활성 상태
    val lastUpdated: String = LocalDateTime.now().toString()
) {
    /**
     * T값 계산 (소수점 둘째자리 올림)
     * T = 매수누적액 / 1회매수액
     */
    fun calculateTValue(): Double {
        if (oneTimeBuyAmount == 0.0) return 0.0
        val rawT = accumulatedInvestment / oneTimeBuyAmount
        return ceil(rawT * 100) / 100 // 소수점 둘째자리 올림
    }

    /**
     * 현재 별% 계산
     */
    fun calculateStarPercent(): Double {
        return strategy.calculateStarPercent(calculateTValue())
    }

    /**
     * 현재 거래 단계 판단
     */
    fun getCurrentPhase(): TradingPhase {
        val t = calculateTValue()
        return when {
            t < 10.0 -> TradingPhase.FIRST_HALF
            t <= 19.0 -> TradingPhase.SECOND_HALF
            else -> TradingPhase.QUARTER_MODE
        }
    }

    /**
     * 현재 수익률 계산
     */
    fun calculateProfitRate(currentPrice: Double): Double {
        if (averagePrice == 0.0 || accumulatedQuantity == 0.0) return 0.0
        return ((currentPrice - averagePrice) / averagePrice) * 100
    }

    /**
     * 현재 평가금액 계산
     */
    fun calculateEvaluationAmount(currentPrice: Double): Double {
        return currentPrice * accumulatedQuantity
    }

    /**
     * 현재 손익금액 계산
     */
    fun calculateProfitAmount(currentPrice: Double): Double {
        return calculateEvaluationAmount(currentPrice) - accumulatedInvestment
    }

    /**
     * 매수 후 상태 업데이트
     */
    fun afterBuy(buyPrice: Double, buyQuantity: Double, buyAmount: Double): StrategyState {
        val newQuantity = accumulatedQuantity + buyQuantity
        val newInvestment = accumulatedInvestment + buyAmount
        val newAvgPrice = if (newQuantity > 0) newInvestment / newQuantity else 0.0

        return copy(
            accumulatedInvestment = newInvestment,
            accumulatedQuantity = newQuantity,
            averagePrice = newAvgPrice,
            lastUpdated = LocalDateTime.now().toString()
        )
    }

    /**
     * 매도 후 상태 업데이트 (쿼터매도 또는 전체청산)
     */
    fun afterSell(sellPrice: Double, sellQuantity: Double, sellAmount: Double): StrategyState {
        val newQuantity = accumulatedQuantity - sellQuantity
        val profit = sellAmount - (averagePrice * sellQuantity)

        // 전체 청산 여부 확인
        val isCycleComplete = newQuantity <= 0.0

        return if (isCycleComplete) {
            // 사이클 종료: 초기화 및 수익 반영
            val newTotalProfit = totalProfit + profit + currentCycleProfit
            val newOneTimeBuyAmount = if (profit > 0) {
                strategy.calculateOneTimeBuyAmount(profit)
            } else {
                oneTimeBuyAmount // 손실 시 유지
            }

            copy(
                cycleNumber = cycleNumber + 1,
                accumulatedInvestment = 0.0,
                accumulatedQuantity = 0.0,
                averagePrice = 0.0,
                totalProfit = newTotalProfit,
                currentCycleProfit = 0.0,
                oneTimeBuyAmount = newOneTimeBuyAmount,
                reservedProfit = if (profit > 0) profit / 2.0 else reservedProfit,
                lastUpdated = LocalDateTime.now().toString()
            )
        } else {
            // 부분 매도 (쿼터매도)
            val newInvestment = averagePrice * newQuantity
            copy(
                accumulatedInvestment = newInvestment,
                accumulatedQuantity = newQuantity,
                currentCycleProfit = currentCycleProfit + profit,
                lastUpdated = LocalDateTime.now().toString()
            )
        }
    }

    companion object {
        /**
         * 새로운 전략 상태 생성
         */
        fun create(userId: String, strategy: TradingStrategy): StrategyState {
            return StrategyState(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                strategy = strategy,
                oneTimeBuyAmount = strategy.calculateOneTimeBuyAmount()
            )
        }
    }
}
