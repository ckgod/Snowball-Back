package com.ckgod.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * 주문 정보
 */
@Serializable
data class Order(
    val id: String,                      // 주문 ID
    val strategyStateId: String,         // 전략 상태 ID
    val ticker: String,                  // 종목 코드
    val orderType: OrderType,            // 주문 유형 (LOC, MOC, LIMIT)
    val orderSide: OrderSide,            // 매수/매도
    val targetPrice: Double,             // 목표 가격 (LOC, LIMIT 용)
    val quantity: Double,                // 수량
    val amount: Double,                  // 금액
    val phase: TradingPhase,             // 주문 시점의 거래 단계
    val tValue: Double,                  // 주문 시점의 T값
    val starPercent: Double,             // 주문 시점의 별%
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: String = LocalDateTime.now().toString(),
    val executedAt: String? = null,
    val executedPrice: Double? = null,
    val executedQuantity: Double? = null,
    val note: String? = null
) {
    fun execute(executedPrice: Double, executedQuantity: Double): Order {
        return copy(
            status = OrderStatus.EXECUTED,
            executedAt = LocalDateTime.now().toString(),
            executedPrice = executedPrice,
            executedQuantity = executedQuantity
        )
    }

    fun cancel(reason: String): Order {
        return copy(
            status = OrderStatus.CANCELLED,
            note = reason
        )
    }
}

@Serializable
enum class OrderStatus {
    PENDING,    // 대기 중
    EXECUTED,   // 체결됨
    CANCELLED,  // 취소됨
    FAILED      // 실패
}
