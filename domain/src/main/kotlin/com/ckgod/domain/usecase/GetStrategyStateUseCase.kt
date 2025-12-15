package com.ckgod.domain.usecase

import com.ckgod.domain.model.StrategyState
import com.ckgod.domain.repository.StrategyStateRepository

/**
 * 전략 상태 조회 UseCase
 */
class GetStrategyStateUseCase(
    private val repository: StrategyStateRepository
) {
    suspend operator fun invoke(userId: String, ticker: String): StrategyState? {
        return repository.findActiveByUserIdAndTicker(userId, ticker)
    }

    suspend fun getById(id: String): StrategyState? {
        return repository.findById(id)
    }

    suspend fun getAllByUserId(userId: String): List<StrategyState> {
        return repository.findByUserId(userId)
    }
}
