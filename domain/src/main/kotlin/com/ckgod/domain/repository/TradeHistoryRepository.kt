package com.ckgod.domain.repository

import com.ckgod.domain.model.TradeHistory

/**
 * 거래 이력 저장소
 */
interface TradeHistoryRepository {
    suspend fun save(trade: TradeHistory): TradeHistory
    suspend fun findByStrategyStateId(strategyStateId: String): List<TradeHistory>
    suspend fun findByUserId(userId: String, limit: Int = 100): List<TradeHistory>
}