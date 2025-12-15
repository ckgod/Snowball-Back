package com.ckgod.domain.repository

import com.ckgod.domain.model.StrategyState

/**
 * 전략 상태 저장소
 */
interface StrategyStateRepository {
    suspend fun save(state: StrategyState): StrategyState
    suspend fun findById(id: String): StrategyState?
    suspend fun findByUserId(userId: String): List<StrategyState>
    suspend fun findActiveByUserIdAndTicker(userId: String, ticker: String): StrategyState?
    suspend fun update(state: StrategyState): StrategyState
    suspend fun delete(id: String)
}