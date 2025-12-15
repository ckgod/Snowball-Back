package com.ckgod.database.trading

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

/**
 * 거래 이력 테이블
 */
object TradeHistories : Table("trade_histories") {
    val id = varchar("id", 100)
    val strategyStateId = varchar("strategy_state_id", 100)
    val userId = varchar("user_id", 100)
    val ticker = varchar("ticker", 20)
    val orderSide = varchar("order_side", 10) // BUY, SELL
    val price = double("price")
    val quantity = double("quantity")
    val amount = double("amount")
    val tValue = double("t_value")
    val starPercent = double("star_percent")
    val phase = varchar("phase", 20)
    val profit = double("profit").default(0.0)
    val executedAt = datetime("executed_at")

    override val primaryKey = PrimaryKey(id)
}
