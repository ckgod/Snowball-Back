package com.ckgod.domain.usecase

import com.ckgod.domain.model.MarketPrice
import com.ckgod.domain.model.StrategyState
import com.ckgod.domain.repository.OrderRepository
import com.ckgod.domain.repository.StockRepository
import com.ckgod.domain.repository.TradeHistoryRepository

/**
 * 거래 현황 조회 UseCase
 * 현재 T값, 별%, 잔고, 투자 현황 등을 조회합니다
 */
class GetTradingStatusUseCase(
    private val getStrategyStateUseCase: GetStrategyStateUseCase,
    private val stockRepository: StockRepository,
    private val orderRepository: OrderRepository,
    private val tradeHistoryRepository: TradeHistoryRepository
) {
    suspend operator fun invoke(userId: String, ticker: String): TradingStatus? {
        // 전략 상태 조회
        val state = getStrategyStateUseCase(userId, ticker)
            ?: return null

        // 현재가 조회
        val marketPrice = stockRepository.getStockPrice(
            userId = userId,
            stockCode = ticker,
            exchange = state.strategy.exchange
        )

        // 대기 중인 주문 조회
        val pendingOrders = orderRepository.findPendingOrders(state.id)

        // 거래 이력 조회
        val tradeHistory = tradeHistoryRepository.findByStrategyStateId(state.id)

        return TradingStatus(
            state = state,
            currentPrice = marketPrice,
            pendingOrderCount = pendingOrders.size,
            totalTradeCount = tradeHistory.size,
            tValue = state.calculateTValue(),
            starPercent = state.calculateStarPercent(),
            phase = state.getCurrentPhase(),
            profitRate = marketPrice?.let { state.calculateProfitRate(it.price.toDouble()) } ?: 0.0,
            profitAmount = marketPrice?.let { state.calculateProfitAmount(it.price.toDouble()) } ?: 0.0
        )
    }
}

/**
 * 거래 현황 데이터
 */
data class TradingStatus(
    val state: StrategyState,
    val currentPrice: MarketPrice?,
    val pendingOrderCount: Int,
    val totalTradeCount: Int,
    val tValue: Double,
    val starPercent: Double,
    val phase: com.ckgod.domain.model.TradingPhase,
    val profitRate: Double,
    val profitAmount: Double
) {
    fun toSummary(): String {
        return buildString {
            appendLine("=== ${state.strategy.ticker} 거래 현황 ===")
            appendLine("사이클: ${state.cycleNumber}회차")
            appendLine("T값: ${String.format("%.2f", tValue)}")
            appendLine("별%: ${String.format("%.2f", starPercent)}%")
            appendLine("단계: ${phase.name}")
            appendLine("평단가: $${String.format("%.2f", state.averagePrice)}")
            appendLine("누적수량: ${String.format("%.4f", state.accumulatedQuantity)}")
            appendLine("투자금액: $${String.format("%.2f", state.accumulatedInvestment)}")
            appendLine("1회매수금: $${String.format("%.2f", state.oneTimeBuyAmount)}")
            currentPrice?.let {
                appendLine("현재가: $${it.price}")
                appendLine("수익률: ${String.format("%.2f", profitRate)}%")
                appendLine("손익금: $${String.format("%.2f", profitAmount)}")
            }
            appendLine("누적총수익: $${String.format("%.2f", state.totalProfit)}")
            appendLine("보유수익금: $${String.format("%.2f", state.reservedProfit)}")
            appendLine("대기주문: ${pendingOrderCount}건")
            appendLine("총거래수: ${totalTradeCount}건")
        }
    }
}
