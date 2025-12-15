package com.ckgod.database.trading

import com.ckgod.domain.model.*
import com.ckgod.domain.repository.OrderRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class OrderRepositoryImpl : OrderRepository {

    override suspend fun save(order: Order): Order = transaction {
        Orders.insert {
            it.toRow(order)
        }
        order
    }

    override suspend fun saveAll(orders: List<Order>): List<Order> = transaction {
        orders.forEach { order ->
            Orders.insert {
                it.toRow(order)
            }
        }
        orders
    }

    override suspend fun findById(id: String): Order? = transaction {
        Orders.selectAll().where { Orders.id eq id }
            .singleOrNull()
            ?.let { it.toOrder() }
    }

    override suspend fun findByStrategyStateId(strategyStateId: String): List<Order> = transaction {
        Orders.selectAll().where { Orders.strategyStateId eq strategyStateId }
            .map { it.toOrder() }
    }

    override suspend fun findPendingOrders(strategyStateId: String): List<Order> = transaction {
        Orders.selectAll()
            .where {
                (Orders.strategyStateId eq strategyStateId) and
                (Orders.status eq "PENDING")
            }
            .map { it.toOrder() }
    }

    override suspend fun update(order: Order): Order = transaction {
        Orders.update({ Orders.id eq order.id }) {
            it.toRow(order)
        }
        order
    }

    override suspend fun updateAll(orders: List<Order>): List<Order> = transaction {
        orders.forEach { order ->
            Orders.update({ Orders.id eq order.id }) {
                it.toRow(order)
            }
        }
        orders
    }

    private fun UpdateBuilder<*>.toRow(order: Order) {
        this[Orders.id] = order.id
        this[Orders.strategyStateId] = order.strategyStateId
        this[Orders.ticker] = order.ticker
        this[Orders.orderType] = order.orderType.name
        this[Orders.orderSide] = order.orderSide.name
        this[Orders.targetPrice] = order.targetPrice
        this[Orders.quantity] = order.quantity
        this[Orders.amount] = order.amount
        this[Orders.phase] = order.phase.name
        this[Orders.tValue] = order.tValue
        this[Orders.starPercent] = order.starPercent
        this[Orders.status] = order.status.name
        this[Orders.createdAt] = LocalDateTime.parse(order.createdAt)
        order.executedAt?.let { this[Orders.executedAt] = LocalDateTime.parse(it) }
        order.executedPrice?.let { this[Orders.executedPrice] = it }
        order.executedQuantity?.let { this[Orders.executedQuantity] = it }
        order.note?.let { this[Orders.note] = it }
    }

    private fun org.jetbrains.exposed.v1.core.ResultRow.toOrder(): Order {
        return Order(
            id = this[Orders.id],
            strategyStateId = this[Orders.strategyStateId],
            ticker = this[Orders.ticker],
            orderType = OrderType.valueOf(this[Orders.orderType]),
            orderSide = OrderSide.valueOf(this[Orders.orderSide]),
            targetPrice = this[Orders.targetPrice],
            quantity = this[Orders.quantity],
            amount = this[Orders.amount],
            phase = TradingPhase.valueOf(this[Orders.phase]),
            tValue = this[Orders.tValue],
            starPercent = this[Orders.starPercent],
            status = OrderStatus.valueOf(this[Orders.status]),
            createdAt = this[Orders.createdAt].toString(),
            executedAt = this[Orders.executedAt]?.toString(),
            executedPrice = this[Orders.executedPrice],
            executedQuantity = this[Orders.executedQuantity],
            note = this[Orders.note]
        )
    }
}
