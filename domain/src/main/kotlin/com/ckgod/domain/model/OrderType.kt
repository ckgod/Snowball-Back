package com.ckgod.domain.model

/**
 * 주문 유형
 */
enum class OrderType {
    /** Limit-on-Close: 종가 지정가 주문 */
    LOC,

    /** Market-on-Close: 종가 시장가 주문 */
    MOC,

    /** 지정가 주문 (after 시간외) */
    LIMIT
}

enum class OrderSide {
    BUY,
    SELL
}
