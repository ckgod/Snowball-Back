package com.ckgod.database.trading

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

/**
 * 주문 테이블
 */
object Orders : Table("orders") {
    val id = varchar("id", 100)
    val strategyStateId = varchar("strategy_state_id", 100)
    val ticker = varchar("ticker", 20)
    val orderType = varchar("order_type", 20) // LOC, MOC, LIMIT
    val orderSide = varchar("order_side", 10) // BUY, SELL
    val targetPrice = double("target_price")
    val quantity = double("quantity")
    val amount = double("amount")
    val phase = varchar("phase", 20) // FIRST_HALF, SECOND_HALF, QUARTER_MODE
    val tValue = double("t_value")
    val starPercent = double("star_percent")
    val status = varchar("status", 20).default("PENDING") // PENDING, EXECUTED, CANCELLED, FAILED
    val createdAt = datetime("created_at")
    val executedAt = datetime("executed_at").nullable()
    val executedPrice = double("executed_price").nullable()
    val executedQuantity = double("executed_quantity").nullable()
    val note = varchar("note", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}
