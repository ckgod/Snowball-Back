package com.ckgod.domain.model

/**
 * 무한매수법 거래 단계
 */
enum class TradingPhase {
    /** 전반전: T < 10 */
    FIRST_HALF,

    /** 후반전: 10 ≤ T ≤ 19 */
    SECOND_HALF,

    /** 쿼터모드: 19 < T < 20 */
    QUARTER_MODE
}
