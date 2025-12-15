package com.ckgod.domain.usecase

import com.ckgod.domain.model.StrategyState
import com.ckgod.domain.model.TradingStrategy
import com.ckgod.domain.repository.StrategyStateRepository
import java.util.UUID

/**
 * 전략 생성 UseCase
 */
class CreateStrategyUseCase(
    private val repository: StrategyStateRepository
) {
    suspend operator fun invoke(
        userId: String,
        ticker: String,
        initialCapital: Double,
        divisions: Int = 20
    ): StrategyState {
        // 기존 활성화된 전략이 있는지 확인
        val existingStrategy = repository.findActiveByUserIdAndTicker(userId, ticker)
        if (existingStrategy != null) {
            throw IllegalStateException("이미 활성화된 $ticker 전략이 있습니다: ${existingStrategy.id}")
        }

        // 전략 생성
        val strategy = when (ticker.uppercase()) {
            "TQQQ" -> TradingStrategy.forTQQQ(initialCapital, divisions)
            "SOXL" -> TradingStrategy.forSOXL(initialCapital, divisions)
            else -> throw IllegalArgumentException("지원하지 않는 종목입니다: $ticker")
        }

        // 상태 생성
        val state = StrategyState(
            id = UUID.randomUUID().toString(),
            userId = userId,
            strategy = strategy,
            oneTimeBuyAmount = strategy.calculateOneTimeBuyAmount()
        )

        return repository.save(state)
    }
}
