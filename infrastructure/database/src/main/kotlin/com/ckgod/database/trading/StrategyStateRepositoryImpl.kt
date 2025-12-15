package com.ckgod.database.trading

import com.ckgod.domain.model.StrategyState
import com.ckgod.domain.model.TradingStrategy
import com.ckgod.domain.repository.StrategyStateRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class StrategyStateRepositoryImpl : StrategyStateRepository {

    override suspend fun save(state: StrategyState): StrategyState = transaction {
        StrategyStates.insert {
            it.toRow(state)
        }
        state
    }

    override suspend fun findById(id: String): StrategyState? = transaction {
        StrategyStates.selectAll().where { StrategyStates.id eq id }
                .singleOrNull()?.toStrategyState()
    }

    override suspend fun findByUserId(userId: String): List<StrategyState> = transaction {
        StrategyStates.selectAll().where { StrategyStates.userId eq userId }
            .map { it.toStrategyState() }
    }

    override suspend fun findActiveByUserIdAndTicker(userId: String, ticker: String): StrategyState? = transaction {
        StrategyStates.selectAll()
                .where {
                    (StrategyStates.userId eq userId) and
                    (StrategyStates.ticker eq ticker) and
                    (StrategyStates.isActive eq true)
                }
                .singleOrNull()?.toStrategyState()
    }

    override suspend fun update(state: StrategyState): StrategyState = transaction {
        StrategyStates.update({ StrategyStates.id eq state.id }) {
            it.toRow(state)
        }
        state
    }

    override suspend fun delete(id: String) = transaction {
        StrategyStates.deleteWhere { StrategyStates.id eq id }
        Unit
    }

    private fun UpdateBuilder<*>.toRow(state: StrategyState) {
        this[StrategyStates.id] = state.id
        this[StrategyStates.userId] = state.userId
        this[StrategyStates.ticker] = state.strategy.ticker
        this[StrategyStates.exchange] = state.strategy.exchange
        this[StrategyStates.divisions] = state.strategy.divisions
        this[StrategyStates.basePercent] = state.strategy.basePercent
        this[StrategyStates.targetPercent] = state.strategy.targetPercent
        this[StrategyStates.initialCapital] = state.strategy.initialCapital
        this[StrategyStates.cycleNumber] = state.cycleNumber
        this[StrategyStates.accumulatedInvestment] = state.accumulatedInvestment
        this[StrategyStates.accumulatedQuantity] = state.accumulatedQuantity
        this[StrategyStates.averagePrice] = state.averagePrice
        this[StrategyStates.totalProfit] = state.totalProfit
        this[StrategyStates.currentCycleProfit] = state.currentCycleProfit
        this[StrategyStates.oneTimeBuyAmount] = state.oneTimeBuyAmount
        this[StrategyStates.reservedProfit] = state.reservedProfit
        this[StrategyStates.isActive] = state.isActive
        this[StrategyStates.lastUpdated] = LocalDateTime.parse(state.lastUpdated)
    }

    private fun org.jetbrains.exposed.v1.core.ResultRow.toStrategyState(): StrategyState {
        val strategy = TradingStrategy(
            ticker = this[StrategyStates.ticker],
            exchange = this[StrategyStates.exchange],
            divisions = this[StrategyStates.divisions],
            basePercent = this[StrategyStates.basePercent],
            targetPercent = this[StrategyStates.targetPercent],
            initialCapital = this[StrategyStates.initialCapital]
        )

        return StrategyState(
            id = this[StrategyStates.id],
            userId = this[StrategyStates.userId],
            strategy = strategy,
            cycleNumber = this[StrategyStates.cycleNumber],
            accumulatedInvestment = this[StrategyStates.accumulatedInvestment],
            accumulatedQuantity = this[StrategyStates.accumulatedQuantity],
            averagePrice = this[StrategyStates.averagePrice],
            totalProfit = this[StrategyStates.totalProfit],
            currentCycleProfit = this[StrategyStates.currentCycleProfit],
            oneTimeBuyAmount = this[StrategyStates.oneTimeBuyAmount],
            reservedProfit = this[StrategyStates.reservedProfit],
            isActive = this[StrategyStates.isActive],
            lastUpdated = this[StrategyStates.lastUpdated].toString()
        )
    }
}
