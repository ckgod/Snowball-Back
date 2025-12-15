package com.ckgod.domain.service

import com.ckgod.domain.model.*
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

/**
 * 주문 생성 서비스
 * 무한매수법의 매수/매도 주문을 생성합니다
 */
class OrderGenerator {

    /**
     * 전반전 매수 주문 생성
     * - 1회매수액의 절반: 별%LOC
     * - 나머지 절반: 0%LOC (평단)
     * - 급락 대비 추가 LOC 매수
     */
    fun generateFirstHalfBuyOrders(
        state: StrategyState,
        currentPrice: Double,
        starPercent: Double
    ): List<Order> {
        val orders = mutableListOf<Order>()
        val oneTimeBuy = state.oneTimeBuyAmount
        val halfAmount = oneTimeBuy / 2.0

        val tValue = state.calculateTValue()

        // 별%LOC 매수 (절반) - 공격형 매수
        // ⚠️ 중요: "시장 현재가" 기준으로 별%만큼 높게 걸어서 "웬만하면 무조건 체결"
        // 예: 시장 현재가 $50, 별% 15% → $50 * (1 + 0.15) = $57.50
        // 상승장에서도 물량 확보, T값 축적이 목적
        val starTargetPrice = currentPrice * (1 + starPercent / 100.0)
        val starQuantity = halfAmount / starTargetPrice
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LOC,
                orderSide = OrderSide.BUY,
                targetPrice = starTargetPrice,
                quantity = starQuantity,
                amount = halfAmount,
                phase = TradingPhase.FIRST_HALF,
                tValue = tValue,
                starPercent = starPercent,
                note = "전반전 별%LOC 매수 (시장 현재가 기준)"
            )
        )

        // 평단(0%)LOC 매수 (절반) - 수비형 매수
        // ⚠️ 중요: "내 평단가" 기준 (첫 매수면 현재가)
        // 주가가 평단보다 쌀 때만 체결되어 평단 상승 방지
        val avgPrice = if (state.averagePrice > 0) state.averagePrice else currentPrice
        val zeroTargetPrice = avgPrice
        val zeroQuantity = halfAmount / zeroTargetPrice
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LOC,
                orderSide = OrderSide.BUY,
                targetPrice = zeroTargetPrice,
                quantity = zeroQuantity,
                amount = halfAmount,
                phase = TradingPhase.FIRST_HALF,
                tValue = tValue,
                starPercent = starPercent,
                note = "전반전 평단LOC 매수 (평단가 기준)"
            )
        )

        // 급락 대비 추가 LOC (5% 간격으로 하방)
        val additionalPrices = generateDownsidePrices(avgPrice, 5, 5.0)
        additionalPrices.forEach { price ->
            orders.add(
                Order(
                    id = UUID.randomUUID().toString(),
                    strategyStateId = state.id,
                    ticker = state.strategy.ticker,
                    orderType = OrderType.LOC,
                    orderSide = OrderSide.BUY,
                    targetPrice = price,
                    quantity = 1.0, // 최소 수량
                    amount = price,
                    phase = TradingPhase.FIRST_HALF,
                    tValue = tValue,
                    starPercent = starPercent,
                    note = "급락대비 추가매수"
                )
            )
        }

        return orders
    }

    /**
     * 후반전 매수 주문 생성
     * - 1회매수액 전체: 별%LOC
     * - 급락 대비 추가 LOC 매수
     */
    fun generateSecondHalfBuyOrders(
        state: StrategyState,
        currentPrice: Double,
        starPercent: Double
    ): List<Order> {
        val orders = mutableListOf<Order>()
        val oneTimeBuy = state.oneTimeBuyAmount

        val tValue = state.calculateTValue()

        // 별%LOC 매수 (전체) - 후반전도 시장 현재가 기준
        // ⚠️ 후반전에서는 별%가 0 이하가 될 수 있음 (음수)
        // 예: T=10 → 별% = 0%, T=12 → 별% = -3%
        // 별%가 음수여도 시장 현재가 기준이므로 체결 가능
        // 예: 현재가 $50, 별% -3% → $50 * (1 - 0.03) = $48.50
        val starTargetPrice = currentPrice * (1 + starPercent / 100.0)
        val starQuantity = oneTimeBuy / starTargetPrice
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LOC,
                orderSide = OrderSide.BUY,
                targetPrice = starTargetPrice,
                quantity = starQuantity,
                amount = oneTimeBuy,
                phase = TradingPhase.SECOND_HALF,
                tValue = tValue,
                starPercent = starPercent,
                note = "후반전 별%LOC 매수 (시장 현재가 기준)"
            )
        )

        // 급락 대비 추가 LOC (5% 간격으로 하방)
        val additionalPrices = generateDownsidePrices(starTargetPrice, 4, 5.0)
        additionalPrices.forEach { price ->
            orders.add(
                Order(
                    id = UUID.randomUUID().toString(),
                    strategyStateId = state.id,
                    ticker = state.strategy.ticker,
                    orderType = OrderType.LOC,
                    orderSide = OrderSide.BUY,
                    targetPrice = price,
                    quantity = 1.0,
                    amount = price,
                    phase = TradingPhase.SECOND_HALF,
                    tValue = tValue,
                    starPercent = starPercent,
                    note = "급락대비 추가매수"
                )
            )
        }

        return orders
    }

    /**
     * 일반 매도 주문 생성 (T ≤ 19)
     * - 누적수량의 1/4: 별%LOC 매도
     * - 누적수량의 3/4: 목표% 지정가 매도
     */
    fun generateNormalSellOrders(
        state: StrategyState,
        currentPrice: Double,
        starPercent: Double
    ): List<Order> {
        val orders = mutableListOf<Order>()
        val totalQuantity = state.accumulatedQuantity

        if (totalQuantity <= 0) return orders

        val quarterQuantity = totalQuantity / 4.0
        val threeQuarterQuantity = totalQuantity - quarterQuantity

        val tValue = state.calculateTValue()
        val avgPrice = state.averagePrice

        // 별%LOC 매도 (1/4) - 쿼터매도
        // 평단가 기준으로 별%만큼 상승 지점에서 매도
        // 별%가 음수일 때는 abs() 사용하여 양수로 변환
        val starTargetPrice = avgPrice * (1 + kotlin.math.abs(starPercent) / 100.0)
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LOC,
                orderSide = OrderSide.SELL,
                targetPrice = starTargetPrice,
                quantity = quarterQuantity,
                amount = starTargetPrice * quarterQuantity,
                phase = state.getCurrentPhase(),
                tValue = tValue,
                starPercent = starPercent,
                note = "쿼터매도(1/4) 별%LOC"
            )
        )

        // 목표% 지정가 매도 (3/4)
        val targetPrice = avgPrice * (1 + state.strategy.targetPercent / 100.0)
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LIMIT,
                orderSide = OrderSide.SELL,
                targetPrice = targetPrice,
                quantity = threeQuarterQuantity,
                amount = targetPrice * threeQuarterQuantity,
                phase = state.getCurrentPhase(),
                tValue = tValue,
                starPercent = starPercent,
                note = "목표${state.strategy.targetPercent}% 지정가매도(3/4)"
            )
        )

        return orders
    }

    /**
     * 쿼터모드 매도 주문 생성 (19 < T < 20)
     * - 누적수량의 1/4: MOC 매도 (무조건 매도)
     * - 누적수량의 3/4: 목표% 지정가 매도
     */
    fun generateQuarterModeSellOrders(
        state: StrategyState,
        currentPrice: Double
    ): List<Order> {
        val orders = mutableListOf<Order>()
        val totalQuantity = state.accumulatedQuantity

        if (totalQuantity <= 0) return orders

        val quarterQuantity = totalQuantity / 4.0
        val threeQuarterQuantity = totalQuantity - quarterQuantity

        val tValue = state.calculateTValue()
        val avgPrice = state.averagePrice

        // MOC 매도 (1/4, 무조건 매도)
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.MOC,
                orderSide = OrderSide.SELL,
                targetPrice = currentPrice, // 참고용
                quantity = quarterQuantity,
                amount = currentPrice * quarterQuantity,
                phase = TradingPhase.QUARTER_MODE,
                tValue = tValue,
                starPercent = state.calculateStarPercent(),
                note = "쿼터모드 MOC매도(1/4)"
            )
        )

        // 목표% 지정가 매도 (3/4)
        val targetPrice = avgPrice * (1 + state.strategy.targetPercent / 100.0)
        orders.add(
            Order(
                id = UUID.randomUUID().toString(),
                strategyStateId = state.id,
                ticker = state.strategy.ticker,
                orderType = OrderType.LIMIT,
                orderSide = OrderSide.SELL,
                targetPrice = targetPrice,
                quantity = threeQuarterQuantity,
                amount = targetPrice * threeQuarterQuantity,
                phase = TradingPhase.QUARTER_MODE,
                tValue = tValue,
                starPercent = state.calculateStarPercent(),
                note = "쿼터모드 목표${state.strategy.targetPercent}% 지정가매도(3/4)"
            )
        )

        return orders
    }

    /**
     * 하방 가격 생성 (급락 대비용)
     */
    private fun generateDownsidePrices(
        basePrice: Double,
        count: Int,
        intervalPercent: Double
    ): List<Double> {
        val prices = mutableListOf<Double>()
        var currentPrice = basePrice

        repeat(count) {
            currentPrice *= (1 - intervalPercent / 100.0)
            prices.add(max(currentPrice, 0.01)) // 최소 0.01
        }

        return prices
    }
}
