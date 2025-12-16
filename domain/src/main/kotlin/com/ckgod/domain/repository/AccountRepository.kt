package com.ckgod.domain.repository

import com.ckgod.domain.model.AccountStatus
import com.ckgod.domain.model.StockHolding

interface AccountRepository {
    suspend fun getAccountBalance(): AccountStatus

    /**
     * 특정 티커의 보유 정보 조회
     */
    suspend fun getBalance(ticker: String): StockHolding? {
        val account = getAccountBalance()
        return account.holdings.find { it.ticker == ticker }
    }

    /**
     * 일일 수익 조회
     */
    suspend fun getDailyProfit(ticker: String): Double
}