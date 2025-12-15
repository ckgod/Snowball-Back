package com.ckgod.domain.service

import com.ckgod.domain.model.*

/**
 * 무한매수법 전략 인터페이스
 */
interface InfinityBuyStrategy {
    /**
     * 매매 결정 생성
     */
    fun decide(state: StrategyState, currentPrice: Double): TradingDecision

    /**
     * 전략 유효성 검증
     */
    fun validate(state: StrategyState): Boolean
}

/**
 * 무한매수법 기본 전략 구현
 */
class DefaultInfinityBuyStrategy(
    private val orderGenerator: OrderGenerator = OrderGenerator()
) : InfinityBuyStrategy {

    override fun decide(state: StrategyState, currentPrice: Double): TradingDecision {
        if (!state.isActive) {
            return TradingDecision(
                state = state,
                currentPrice = currentPrice,
                buyOrders = emptyList(),
                sellOrders = emptyList(),
                reason = "전략이 비활성 상태입니다"
            )
        }

        val tValue = state.calculateTValue()
        val starPercent = state.calculateStarPercent()
        val phase = state.getCurrentPhase()

        // 쿼터모드
        if (phase == TradingPhase.QUARTER_MODE) {
            val sellOrders = orderGenerator.generateQuarterModeSellOrders(state, currentPrice)
            return TradingDecision(
                state = state,
                currentPrice = currentPrice,
                buyOrders = emptyList(), // 쿼터모드에서는 매수 없음
                sellOrders = sellOrders,
                reason = "쿼터모드: T=$tValue, 별%=$starPercent, MOC매도 실행"
            )
        }

        // 매수 주문 생성
        val buyOrders = when (phase) {
            TradingPhase.FIRST_HALF -> orderGenerator.generateFirstHalfBuyOrders(
                state, currentPrice, starPercent
            )
            TradingPhase.SECOND_HALF -> orderGenerator.generateSecondHalfBuyOrders(
                state, currentPrice, starPercent
            )
            else -> emptyList()
        }

        // 매도 주문 생성 (포지션이 있을 때만)
        val sellOrders = if (state.accumulatedQuantity > 0) {
            orderGenerator.generateNormalSellOrders(state, currentPrice, starPercent)
        } else {
            emptyList()
        }

        val reason = buildString {
            append("${phase.name}: T=${String.format("%.2f", tValue)}, ")
            append("별%=${String.format("%.2f", starPercent)}%, ")
            append("평단=$${String.format("%.2f", state.averagePrice)}, ")
            append("누적수량=${String.format("%.2f", state.accumulatedQuantity)}")
        }

        return TradingDecision(
            state = state,
            currentPrice = currentPrice,
            buyOrders = buyOrders,
            sellOrders = sellOrders,
            reason = reason
        )
    }

    override fun validate(state: StrategyState): Boolean {
        if (state.strategy.divisions <= 0) return false
        if (state.strategy.basePercent <= 0) return false
        if (state.oneTimeBuyAmount <= 0) return false
        return true
    }
}
