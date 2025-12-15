package com.ckgod.domain.repository

import com.ckgod.domain.model.Order

/**
 * 주문 저장소
 */
interface OrderRepository {
    suspend fun save(order: Order): Order
    suspend fun saveAll(orders: List<Order>): List<Order>
    suspend fun findById(id: String): Order?
    suspend fun findByStrategyStateId(strategyStateId: String): List<Order>
    suspend fun findPendingOrders(strategyStateId: String): List<Order>
    suspend fun update(order: Order): Order
    suspend fun updateAll(orders: List<Order>): List<Order>
}




