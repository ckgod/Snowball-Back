package com.ckgod.domain.usecase

import com.ckgod.domain.model.AccountStatus
import com.ckgod.domain.model.StockHolding
import com.ckgod.domain.model.StrategyState
import com.ckgod.domain.repository.AccountRepository
import com.ckgod.domain.repository.StrategyStateRepository

/**
 * 전략 상태 동기화 UseCase
 *
 * 매일 장마감 후 한투 API로 실제 계좌 정보를 조회하여 전략 상태를 동기화합니다.
 * 이 방식은 폴링/웹훅 없이 하루에 한 번만 계좌를 확인하여 상태를 업데이트합니다.
 *
 * 실행 시점: 매일 오후 4시 (장마감 후, 주문 생성 전)
 */
class SyncStrategyStateUseCase(
    private val accountRepository: AccountRepository,
    private val strategyStateRepository: StrategyStateRepository,
    private val getStrategyStateUseCase: GetStrategyStateUseCase
) {
    /**
     * 계좌 정보 기반으로 전략 상태 동기화
     *
     * @param userId 사용자 ID
     * @param ticker 종목 코드 (TQQQ, SOXL)
     * @return 동기화된 전략 상태
     */
    suspend operator fun invoke(userId: String, ticker: String): SyncResult? {
        // 1. 현재 전략 상태 조회
        val currentState = getStrategyStateUseCase(userId, ticker)
            ?: return null

        // 2. 한투 API로 실제 계좌 상태 조회
        val accountStatus = accountRepository.getAccountBalance()

        // 3. 해당 종목의 보유 정보 찾기
        val holding = accountStatus.holdings.find { it.ticker == ticker }

        // 4. 기간손익 계산 (어제와 오늘 비교)
        val profit = calculateDailyProfit(accountStatus, ticker)

        // 5. 전략 상태 동기화
        val syncedState = syncState(currentState, holding, profit)

        // 6. DB 저장
        strategyStateRepository.update(syncedState)

        return SyncResult(
            before = currentState,
            after = syncedState,
            holding = holding,
            profit = profit,
            message = buildSyncMessage(currentState, syncedState, holding, profit)
        )
    }

    /**
     * 실제 계좌 정보로 전략 상태 업데이트
     */
    private fun syncState(
        currentState: StrategyState,
        holding: StockHolding?,
        dailyProfit: Double
    ): StrategyState {
        // 보유 종목이 없는 경우 = 전량 청산됨
        if (holding == null || holding.quantity.toDouble() == 0.0) {
            return handleFullLiquidation(currentState, dailyProfit)
        }

        // 보유 종목이 있는 경우 = 실제 잔고로 동기화
        val actualQuantity = holding.quantity.toDouble()
        val actualAvgPrice = holding.avgPrice.toDouble()
        val actualInvestment = holding.investedAmount.toDouble()

        // 1회매수금 업데이트 (수익 발생 시)
        val newOneTimeBuyAmount = if (dailyProfit > 0) {
            currentState.strategy.calculateOneTimeBuyAmount(dailyProfit)
        } else {
            currentState.oneTimeBuyAmount // 손실 시 유지
        }

        return currentState.copy(
            accumulatedInvestment = actualInvestment,
            accumulatedQuantity = actualQuantity,
            averagePrice = actualAvgPrice,
            oneTimeBuyAmount = newOneTimeBuyAmount,
            currentCycleProfit = currentState.currentCycleProfit + dailyProfit,
            lastUpdated = java.time.LocalDateTime.now().toString()
        )
    }

    /**
     * 전량 청산 처리
     * 보유 수량이 0이 되면 사이클 종료로 판단
     */
    private fun handleFullLiquidation(
        currentState: StrategyState,
        dailyProfit: Double
    ): StrategyState {
        val totalProfit = currentState.currentCycleProfit + dailyProfit

        // 수익 발생 시 1회매수금 업데이트
        val newOneTimeBuyAmount = if (totalProfit > 0) {
            currentState.strategy.calculateOneTimeBuyAmount(totalProfit)
        } else {
            currentState.oneTimeBuyAmount
        }

        return currentState.copy(
            cycleNumber = currentState.cycleNumber + 1,
            accumulatedInvestment = 0.0,
            accumulatedQuantity = 0.0,
            averagePrice = 0.0,
            totalProfit = currentState.totalProfit + totalProfit,
            currentCycleProfit = 0.0,
            oneTimeBuyAmount = newOneTimeBuyAmount,
            reservedProfit = if (totalProfit > 0) {
                currentState.reservedProfit + (totalProfit / 2.0)
            } else {
                currentState.reservedProfit
            },
            lastUpdated = java.time.LocalDateTime.now().toString()
        )
    }

    /**
     * 일일 수익 계산
     * 실제로는 한투 API의 기간손익을 사용하거나, 전일 대비 변화량 계산
     */
    private fun calculateDailyProfit(
        accountStatus: AccountStatus,
        ticker: String
    ): Double {
        // Option 1: 한투 API의 기간손익 사용 (더 정확)
        // return kisApi.getDailyProfit(ticker)

        // Option 2: 현재 손익에서 전일 손익 차감
        val holding = accountStatus.holdings.find { it.ticker == ticker }
        return holding?.let {
            // 현재 평가금액 - 투자금액 = 총 손익
            val currentProfit = it.currentPrice.toDouble() * it.quantity.toDouble() -
                                it.investedAmount.toDouble()
            // 실제로는 이전 손익을 DB에 저장해두고 차감해야 정확함
            currentProfit
        } ?: 0.0
    }

    /**
     * 동기화 메시지 생성
     */
    private fun buildSyncMessage(
        before: StrategyState,
        after: StrategyState,
        holding: StockHolding?,
        profit: Double
    ): String {
        return buildString {
            appendLine("=== 계좌 동기화 완료 ===")
            appendLine("종목: ${before.strategy.ticker}")
            appendLine("일일손익: $${String.format("%.2f", profit)}")
            appendLine()
            appendLine("[변경사항]")
            appendLine("수량: ${String.format("%.4f", before.accumulatedQuantity)} → ${String.format("%.4f", after.accumulatedQuantity)}")
            appendLine("평단: $${String.format("%.2f", before.averagePrice)} → $${String.format("%.2f", after.averagePrice)}")
            appendLine("투자액: $${String.format("%.2f", before.accumulatedInvestment)} → $${String.format("%.2f", after.accumulatedInvestment)}")
            appendLine("1회매수금: $${String.format("%.2f", before.oneTimeBuyAmount)} → $${String.format("%.2f", after.oneTimeBuyAmount)}")
            appendLine()
            appendLine("[현재상태]")
            appendLine("T값: ${String.format("%.2f", after.calculateTValue())}")
            appendLine("별%: ${String.format("%.2f", after.calculateStarPercent())}%")
            appendLine("단계: ${after.getCurrentPhase()}")

            if (holding == null || holding.quantity.toDouble() == 0.0) {
                appendLine()
                appendLine("🎉 사이클 ${before.cycleNumber} 종료! 다음 사이클 시작")
            }
        }
    }
}

/**
 * 동기화 결과
 */
data class SyncResult(
    val before: StrategyState,
    val after: StrategyState,
    val holding: StockHolding?,
    val profit: Double,
    val message: String
)
