package com.ckgod.domain.usecase

import com.ckgod.domain.model.Order
import com.ckgod.domain.model.TradingDecision
import com.ckgod.domain.repository.OrderRepository
import com.ckgod.domain.repository.StockRepository
import com.ckgod.domain.service.DefaultInfinityBuyStrategy
import com.ckgod.domain.service.InfinityBuyStrategy

/**
 * 주문 생성 UseCase
 *
 * 매일 오후 5-6시 실행되어 당일 주문을 생성합니다.
 * 생성된 주문은 참고용으로 DB에 저장하고, 실제 API로 전송합니다.
 *
 * 주의: 주문 체결 추적은 하지 않습니다.
 * 대신 다음날 SyncStrategyStateUseCase로 계좌를 동기화합니다.
 */
class GenerateOrdersUseCase(
    private val stockRepository: StockRepository,
    private val orderRepository: OrderRepository,
    private val getStrategyStateUseCase: GetStrategyStateUseCase,
    private val strategy: InfinityBuyStrategy = DefaultInfinityBuyStrategy()
) {
    /**
     * 주문 생성
     *
     * @param userId 사용자 ID
     * @param ticker 종목 코드
     * @return 생성된 주문 리스트
     */
    suspend operator fun invoke(
        userId: String,
        ticker: String
    ): OrderGenerationResult? {
        // 1. 전략 상태 조회
        val state = getStrategyStateUseCase(userId, ticker)
            ?: return null

        if (!state.isActive) {
            return OrderGenerationResult(
                decision = null,
                orders = emptyList(),
                message = "전략이 비활성 상태입니다"
            )
        }

        // 2. 현재가 조회
        val marketPrice = stockRepository.getStockPrice(
            userId = userId,
            stockCode = ticker,
            exchange = state.strategy.exchange
        ) ?: throw IllegalStateException("현재가를 조회할 수 없습니다: $ticker")

        val currentPrice = marketPrice.price.toDouble()

        // 3. 매매 결정
        val decision = strategy.decide(state, currentPrice)

        // 4. 주문 DB 저장 (참고용)
        val allOrders = decision.buyOrders + decision.sellOrders
        if (allOrders.isNotEmpty()) {
            orderRepository.saveAll(allOrders)
        }

        // 5. 결과 반환
        val message = buildOrderMessage(decision, currentPrice)

        return OrderGenerationResult(
            decision = decision,
            orders = allOrders,
            message = message
        )
    }

    /**
     * 주문 메시지 생성
     */
    private fun buildOrderMessage(decision: TradingDecision, currentPrice: Double): String {
        return buildString {
            appendLine("=== ${decision.state.strategy.ticker} 주문 생성 ===")
            appendLine("현재가: $${String.format("%.2f", currentPrice)}")
            appendLine("T값: ${String.format("%.2f", decision.state.calculateTValue())}")
            appendLine("별%: ${String.format("%.2f", decision.state.calculateStarPercent())}%")
            appendLine("단계: ${decision.state.getCurrentPhase()}")
            appendLine()
            appendLine("[매수 주문: ${decision.buyOrders.size}건]")
            decision.buyOrders.forEach { order ->
                appendLine("  - ${order.orderType}: ${String.format("%.4f", order.quantity)}주 @ $${String.format("%.2f", order.targetPrice)}")
            }
            appendLine()
            appendLine("[매도 주문: ${decision.sellOrders.size}건]")
            decision.sellOrders.forEach { order ->
                appendLine("  - ${order.orderType}: ${String.format("%.4f", order.quantity)}주 @ $${String.format("%.2f", order.targetPrice)}")
            }
            appendLine()
            appendLine("사유: ${decision.reason}")
        }
    }
}

/**
 * 주문 생성 결과
 */
data class OrderGenerationResult(
    val decision: TradingDecision?,
    val orders: List<Order>,
    val message: String
) {
    val hasBuyOrders: Boolean
        get() = decision?.buyOrders?.isNotEmpty() ?: false

    val hasSellOrders: Boolean
        get() = decision?.sellOrders?.isNotEmpty() ?: false
}
