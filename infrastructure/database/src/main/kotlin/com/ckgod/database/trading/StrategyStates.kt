package com.ckgod.database.trading

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

/**
 * 전략 상태 테이블
 */
object StrategyStates : Table("strategy_states") {
    val id = varchar("id", 100)
    val userId = varchar("user_id", 100)
    val ticker = varchar("ticker", 20)
    val exchange = varchar("exchange", 50)
    val divisions = integer("divisions")
    val basePercent = double("base_percent")
    val targetPercent = double("target_percent")
    val initialCapital = double("initial_capital")

    val cycleNumber = integer("cycle_number").default(1)
    val accumulatedInvestment = double("accumulated_investment").default(0.0)
    val accumulatedQuantity = double("accumulated_quantity").default(0.0)
    val averagePrice = double("average_price").default(0.0)
    val totalProfit = double("total_profit").default(0.0)
    val currentCycleProfit = double("current_cycle_profit").default(0.0)
    val oneTimeBuyAmount = double("one_time_buy_amount")
    val reservedProfit = double("reserved_profit").default(0.0)
    val isActive = bool("is_active").default(true)
    val lastUpdated = datetime("last_updated")

    override val primaryKey = PrimaryKey(id)
}
