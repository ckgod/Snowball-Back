package com.ckgod.database.trading

import com.ckgod.domain.model.TradeHistory
import com.ckgod.domain.repository.TradeHistoryRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime

class TradeHistoryRepositoryImpl : TradeHistoryRepository {

    override suspend fun save(trade: TradeHistory): TradeHistory = transaction {
        TradeHistories.insert {
            it.toRow(trade)
        }
        trade
    }

    override suspend fun findByStrategyStateId(strategyStateId: String): List<TradeHistory> = transaction {
        TradeHistories.selectAll()
            .where { TradeHistories.strategyStateId eq strategyStateId }
            .orderBy(TradeHistories.executedAt)
            .map { it.toTradeHistory() }
    }

    override suspend fun findByUserId(userId: String, limit: Int): List<TradeHistory> = transaction {
        TradeHistories.selectAll()
            .where { TradeHistories.userId eq userId }
            .orderBy(TradeHistories.executedAt)
            .limit(limit)
            .map { it.toTradeHistory() }
    }

    private fun UpdateBuilder<*>.toRow(trade: TradeHistory) {
        this[TradeHistories.id] = trade.id
        this[TradeHistories.strategyStateId] = trade.strategyStateId
        this[TradeHistories.userId] = trade.userId
        this[TradeHistories.ticker] = trade.ticker
        this[TradeHistories.orderSide] = trade.orderSide
        this[TradeHistories.price] = trade.price
        this[TradeHistories.quantity] = trade.quantity
        this[TradeHistories.amount] = trade.amount
        this[TradeHistories.tValue] = trade.tValue
        this[TradeHistories.starPercent] = trade.starPercent
        this[TradeHistories.phase] = trade.phase
        this[TradeHistories.profit] = trade.profit
        this[TradeHistories.executedAt] = LocalDateTime.parse(trade.executedAt)
    }

    private fun org.jetbrains.exposed.v1.core.ResultRow.toTradeHistory(): TradeHistory {
        return TradeHistory(
            id = this[TradeHistories.id],
            strategyStateId = this[TradeHistories.strategyStateId],
            userId = this[TradeHistories.userId],
            ticker = this[TradeHistories.ticker],
            orderSide = this[TradeHistories.orderSide],
            price = this[TradeHistories.price],
            quantity = this[TradeHistories.quantity],
            amount = this[TradeHistories.amount],
            tValue = this[TradeHistories.tValue],
            starPercent = this[TradeHistories.starPercent],
            phase = this[TradeHistories.phase],
            profit = this[TradeHistories.profit],
            executedAt = this[TradeHistories.executedAt].toString()
        )
    }
}
