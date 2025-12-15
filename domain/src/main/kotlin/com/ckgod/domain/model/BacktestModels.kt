package com.ckgod.domain.model

import java.time.LocalDate

/**
 * 가격 데이터 (일일)
 */
data class PriceData(
    val ticker: String,
    val date: LocalDate,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * 백테스팅 결과
 */
data class BacktestResult(
    val ticker: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val initialCapital: Double,
    val finalCapital: Double,
    val totalProfit: Double,
    val profitRate: Double,
    val cycles: Int,
    val dailyResults: List<DailyBacktestResult>,
    val summary: BacktestSummary
) {
    fun toReport(): String {
        return buildString {
            appendLine("=" * 60)
            appendLine("백테스팅 결과 리포트")
            appendLine("=" * 60)
            appendLine()
            appendLine("📊 기본 정보")
            appendLine("  종목: $ticker")
            appendLine("  기간: $startDate ~ $endDate (${dailyResults.size}일)")
            appendLine("  초기 자본: $${"%.2f".format(initialCapital)}")
            appendLine("  최종 자산: $${"%.2f".format(finalCapital)}")
            appendLine()
            appendLine("💰 수익 정보")
            appendLine("  총 수익: $${"%.2f".format(totalProfit)}")
            appendLine("  수익률: ${"%.2f".format(profitRate)}%")
            appendLine("  완료 사이클: ${cycles}회")
            appendLine()
            appendLine("📈 성과 지표")
            appendLine("  최대 수익: $${"%.2f".format(summary.maxProfit)}")
            appendLine("  최대 손실: $${"%.2f".format(summary.maxDrawdown)}")
            appendLine("  승률: ${"%.2f".format(summary.winRate)}%")
            appendLine("  평균 일일 수익률: ${"%.4f".format(summary.avgDailyReturn)}%")
            appendLine()
            appendLine("🔢 거래 통계")
            appendLine("  총 매수 횟수: ${summary.totalBuys}")
            appendLine("  총 매도 횟수: ${summary.totalSells}")
            appendLine("  평균 보유 기간: ${"%.1f".format(summary.avgHoldingDays)}일")
            appendLine()
            appendLine("=" * 60)
            appendLine()
            appendLine("📅 일별 거래 내역")
            appendLine("=" * 60)
            appendLine()

            dailyResults.forEach { day ->
                val phaseStr = when (day.phase.name) {
                    "FIRST_HALF" -> "전반전"
                    "SECOND_HALF" -> "후반전"
                    "QUARTER_MODE" -> "쿼터모드"
                    else -> day.phase.name
                }

                appendLine("[ ${day.date} ]")
                appendLine("  가격: $${"%.2f".format(day.price)} | T값: ${"%.2f".format(day.tValue)} | 별%: ${"%.2f".format(day.starPercent)}% | 단계: $phaseStr")
                appendLine("  보유: ${day.quantity.toInt()}주 @$${"%.2f".format(day.avgPrice)} | 투자금: $${"%.0f".format(day.investment)}")
                appendLine("  평가금: $${"%.0f".format(day.evaluation)} | 손익: $${"%.0f".format(day.profit)} (${"%.2f".format(day.profitRate)}%)")
                appendLine("  1회매수금: $${"%.2f".format(day.oneTimeBuyAmount)} | 주문: 매수${day.buyOrders}건 매도${day.sellOrders}건")

                if (day.executed.isNotEmpty()) {
                    appendLine("  체결 내역:")
                    day.executed.forEach { log ->
                        appendLine("    $log")
                    }
                }
                appendLine()
            }

            appendLine("=" * 60)
        }
    }
}

/**
 * 일일 백테스팅 결과
 */
data class DailyBacktestResult(
    val date: LocalDate,
    val price: Double,
    val tValue: Double,
    val starPercent: Double,
    val phase: TradingPhase,
    val quantity: Double,
    val avgPrice: Double,
    val investment: Double,
    val evaluation: Double,
    val profit: Double,
    val profitRate: Double,
    val oneTimeBuyAmount: Double,
    val buyOrders: Int,
    val sellOrders: Int,
    val executed: List<String>
)

/**
 * 백테스팅 요약
 */
data class BacktestSummary(
    val maxProfit: Double,
    val maxDrawdown: Double,
    val winRate: Double,
    val avgDailyReturn: Double,
    val totalBuys: Int,
    val totalSells: Int,
    val avgHoldingDays: Double,
    val sharpeRatio: Double = 0.0,
    val maxConsecutiveLosses: Int = 0
)

operator fun String.times(count: Int): String = this.repeat(count)
