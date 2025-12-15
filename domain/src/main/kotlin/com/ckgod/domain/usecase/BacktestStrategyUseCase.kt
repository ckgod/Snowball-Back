package com.ckgod.domain.usecase

import com.ckgod.domain.model.*
import com.ckgod.domain.repository.PriceDataRepository
import com.ckgod.domain.service.OrderGenerator
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * 백테스팅 UseCase
 * 과거 가격 데이터를 사용하여 무한매수법 전략을 시뮬레이션합니다
 */
class BacktestStrategyUseCase(
    private val priceDataRepository: PriceDataRepository,
    private val orderGenerator: OrderGenerator
) {

    /**
     * 백테스팅 실행
     * @param strategy 전략 설정
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 백테스팅 결과
     */
    suspend operator fun invoke(
        strategy: TradingStrategy,
        startDate: LocalDate,
        endDate: LocalDate
    ): BacktestResult {
        // 1. 가격 데이터 로드
        val priceData = priceDataRepository.getPriceData(strategy.ticker, startDate, endDate)
        if (priceData.isEmpty()) {
            throw IllegalArgumentException("가격 데이터가 없습니다: ${strategy.ticker} ($startDate ~ $endDate)")
        }

        // 2. 초기 상태 생성
        var state = StrategyState.create(
            userId = "backtest",
            strategy = strategy
        )

        // 3. 일별 결과 추적
        val dailyResults = mutableListOf<DailyBacktestResult>()
        var cycleCount = 0
        var cash = strategy.initialCapital  // 현금 추적

        // 4. 각 거래일 시뮬레이션
        priceData.forEach { priceDay ->
            val currentPrice = priceDay.close
            val starPercent = state.calculateStarPercent()
            val tValue = state.calculateTValue()
            val phase = state.getCurrentPhase()

            // 주문 생성 (17:00 시뮬레이션)
            val orders = generateOrders(state, currentPrice, starPercent)

            // 주문 체결 시뮬레이션
            val executionResult = simulateOrderExecution(orders, priceDay, state, cash)

            // 상태 및 현금 업데이트
            state = executionResult.updatedState
            cash = executionResult.updatedCash

            // 일일 결과 기록
            dailyResults.add(
                DailyBacktestResult(
                    date = priceDay.date,
                    price = currentPrice,
                    tValue = tValue,
                    starPercent = starPercent,
                    phase = phase,
                    quantity = state.accumulatedQuantity,
                    avgPrice = state.averagePrice,
                    investment = state.accumulatedInvestment,
                    evaluation = state.accumulatedQuantity * currentPrice,
                    profit = (state.accumulatedQuantity * currentPrice) - state.accumulatedInvestment,
                    profitRate = if (state.accumulatedInvestment > 0)
                        ((state.accumulatedQuantity * currentPrice - state.accumulatedInvestment) / state.accumulatedInvestment) * 100
                    else 0.0,
                    oneTimeBuyAmount = state.oneTimeBuyAmount,
                    buyOrders = executionResult.executedBuys,
                    sellOrders = executionResult.executedSells,
                    executed = executionResult.executionLog
                )
            )

            // 사이클 완료 체크 (전량 매도되어 잔고가 0이 된 경우)
            if (executionResult.cycleCompleted) {
                cycleCount++
            }
        }

        // 5. 최종 평가
        val lastPrice = priceData.last().close
        val finalCapital = cash + (state.accumulatedQuantity * lastPrice)
        val totalProfit = finalCapital - strategy.initialCapital
        val profitRate = (totalProfit / strategy.initialCapital) * 100

        // 6. 요약 통계 계산
        val summary = calculateSummary(dailyResults, strategy.initialCapital)

        return BacktestResult(
            ticker = strategy.ticker,
            startDate = startDate,
            endDate = endDate,
            initialCapital = strategy.initialCapital,
            finalCapital = finalCapital,
            totalProfit = totalProfit,
            profitRate = profitRate,
            cycles = cycleCount,
            dailyResults = dailyResults,
            summary = summary
        )
    }

    /**
     * 주문 생성 (전략에 따라)
     */
    private fun generateOrders(
        state: StrategyState,
        currentPrice: Double,
        starPercent: Double
    ): List<Order> {
        val phase = state.getCurrentPhase()
        val tValue = state.calculateTValue()

        return when {
            // 쿼터모드 (19 < T < 20)
            tValue > 19.0 && state.accumulatedQuantity > 0 -> {
                orderGenerator.generateQuarterModeSellOrders(state, currentPrice)
            }
            // 일반 매도 (보유 중이고 T <= 19)
            state.accumulatedQuantity > 0 -> {
                orderGenerator.generateNormalSellOrders(state, currentPrice, starPercent)
            }
            // 전반전 매수
            phase == TradingPhase.FIRST_HALF -> {
                orderGenerator.generateFirstHalfBuyOrders(state, currentPrice, starPercent)
            }
            // 후반전 매수
            phase == TradingPhase.SECOND_HALF -> {
                orderGenerator.generateSecondHalfBuyOrders(state, currentPrice, starPercent)
            }
            else -> emptyList()
        }
    }

    /**
     * 주문 체결 시뮬레이션
     * - LOC 매수: 당일 고가가 목표가 이상이면 체결
     * - LOC 매도: 당일 고가가 목표가 이상이면 체결
     * - MOC: 무조건 체결 (종가)
     * - LIMIT: 당일 고가가 목표가 이상이면 체결
     */
    private fun simulateOrderExecution(
        orders: List<Order>,
        priceDay: PriceData,
        currentState: StrategyState,
        currentCash: Double
    ): ExecutionResult {
        var state = currentState.copy()
        var cash = currentCash
        var executedBuys = 0
        var executedSells = 0
        val executionLog = mutableListOf<String>()
        var cycleCompleted = false

        orders.forEach { order ->
            val executed = when (order.orderType) {
                OrderType.LOC -> {
                    // LOC는 당일 가격 범위 내에서 체결 가능한지 확인
                    when (order.orderSide) {
                        OrderSide.BUY -> {
                            // 매수 LOC: 목표가가 당일 고가 이하면 체결 (현금 충분한 경우만)
                            if (order.targetPrice <= priceDay.high && cash >= order.amount) {
                                state = state.afterBuy(order.targetPrice, order.quantity, order.amount)
                                cash -= order.amount  // 현금 차감
                                executedBuys++
                                executionLog.add("✓ 매수 LOC ${order.quantity.toInt()}주 @$${order.targetPrice} (${order.note})")
                                true
                            } else false
                        }
                        OrderSide.SELL -> {
                            // 매도 LOC: 목표가가 당일 고가 이하면 체결 가능
                            if (order.targetPrice <= priceDay.high && state.accumulatedQuantity >= order.quantity) {
                                val profit = (order.targetPrice - state.averagePrice) * order.quantity
                                val sellAmount = order.targetPrice * order.quantity
                                state = state.afterSell(order.targetPrice, order.quantity, sellAmount)
                                cash += sellAmount  // 현금 증가
                                executedSells++
                                executionLog.add("✓ 매도 LOC ${order.quantity.toInt()}주 @$${order.targetPrice} 수익: $${profit.toInt()} (${order.note})")

                                // 전량 매도 체크
                                if (state.accumulatedQuantity == 0.0) {
                                    cycleCompleted = true
                                }
                                true
                            } else false
                        }
                    }
                }
                OrderType.MOC -> {
                    // MOC는 무조건 종가에 체결
                    when (order.orderSide) {
                        OrderSide.BUY -> {
                            val buyAmount = priceDay.close * order.quantity
                            if (cash >= buyAmount) {
                                state = state.afterBuy(priceDay.close, order.quantity, buyAmount)
                                cash -= buyAmount  // 현금 차감
                                executedBuys++
                                executionLog.add("✓ 매수 MOC ${order.quantity.toInt()}주 @$${priceDay.close} (${order.note})")
                                true
                            } else false
                        }
                        OrderSide.SELL -> {
                            if (state.accumulatedQuantity >= order.quantity) {
                                val profit = (priceDay.close - state.averagePrice) * order.quantity
                                val sellAmount = priceDay.close * order.quantity
                                state = state.afterSell(priceDay.close, order.quantity, sellAmount)
                                cash += sellAmount  // 현금 증가
                                executedSells++
                                executionLog.add("✓ 매도 MOC ${order.quantity.toInt()}주 @$${priceDay.close} 수익: $${profit.toInt()} (${order.note})")

                                if (state.accumulatedQuantity == 0.0) {
                                    cycleCompleted = true
                                }
                                true
                            } else false
                        }
                    }
                }
                OrderType.LIMIT -> {
                    // LIMIT는 목표가 이하로 떨어지면 체결
                    when (order.orderSide) {
                        OrderSide.BUY -> {
                            if (order.targetPrice >= priceDay.low && cash >= order.amount) {
                                state = state.afterBuy(order.targetPrice, order.quantity, order.amount)
                                cash -= order.amount  // 현금 차감
                                executedBuys++
                                executionLog.add("✓ 매수 LIMIT ${order.quantity.toInt()}주 @$${order.targetPrice} (${order.note})")
                                true
                            } else false
                        }
                        OrderSide.SELL -> {
                            if (order.targetPrice <= priceDay.high && state.accumulatedQuantity >= order.quantity) {
                                val profit = (order.targetPrice - state.averagePrice) * order.quantity
                                val sellAmount = order.targetPrice * order.quantity
                                state = state.afterSell(order.targetPrice, order.quantity, sellAmount)
                                cash += sellAmount  // 현금 증가
                                executedSells++
                                executionLog.add("✓ 매도 LIMIT ${order.quantity.toInt()}주 @$${order.targetPrice} 수익: $${profit.toInt()} (${order.note})")

                                if (state.accumulatedQuantity == 0.0) {
                                    cycleCompleted = true
                                }
                                true
                            } else false
                        }
                    }
                }
            }
        }

        return ExecutionResult(
            updatedState = state,
            updatedCash = cash,
            executedBuys = executedBuys,
            executedSells = executedSells,
            executionLog = executionLog,
            cycleCompleted = cycleCompleted
        )
    }

    /**
     * 요약 통계 계산
     */
    private fun calculateSummary(
        dailyResults: List<DailyBacktestResult>,
        initialCapital: Double
    ): BacktestSummary {
        val maxProfit = dailyResults.maxOfOrNull { it.profit } ?: 0.0
        val maxDrawdown = dailyResults.minOfOrNull { it.profit } ?: 0.0

        val profitableDays = dailyResults.count { it.profit > 0 }
        val winRate = if (dailyResults.isNotEmpty())
            (profitableDays.toDouble() / dailyResults.size) * 100
        else 0.0

        val avgDailyReturn = dailyResults
            .map { it.profitRate }
            .average()

        val totalBuys = dailyResults.sumOf { it.buyOrders }
        val totalSells = dailyResults.sumOf { it.sellOrders }

        // 평균 보유 기간 계산 (간단하게 전체 일수 / 매도 횟수)
        val avgHoldingDays = if (totalSells > 0)
            dailyResults.size.toDouble() / totalSells
        else 0.0

        // 샤프 비율 계산 (일일 수익률 기준)
        val dailyReturns = dailyResults.map { it.profitRate / 100.0 }
        val avgReturn = dailyReturns.average()
        val stdDev = kotlin.math.sqrt(
            dailyReturns.map { (it - avgReturn) * (it - avgReturn) }.average()
        )
        val sharpeRatio = if (stdDev > 0) avgReturn / stdDev else 0.0

        // 최대 연속 손실일 계산
        var maxConsecutiveLosses = 0
        var currentLosses = 0
        dailyResults.forEach { day ->
            if (day.profit < 0) {
                currentLosses++
                maxConsecutiveLosses = max(maxConsecutiveLosses, currentLosses)
            } else {
                currentLosses = 0
            }
        }

        return BacktestSummary(
            maxProfit = maxProfit,
            maxDrawdown = maxDrawdown,
            winRate = winRate,
            avgDailyReturn = avgDailyReturn,
            totalBuys = totalBuys,
            totalSells = totalSells,
            avgHoldingDays = avgHoldingDays,
            sharpeRatio = sharpeRatio,
            maxConsecutiveLosses = maxConsecutiveLosses
        )
    }

    /**
     * 주문 체결 결과
     */
    private data class ExecutionResult(
        val updatedState: StrategyState,
        val updatedCash: Double,
        val executedBuys: Int,
        val executedSells: Int,
        val executionLog: List<String>,
        val cycleCompleted: Boolean
    )
}
