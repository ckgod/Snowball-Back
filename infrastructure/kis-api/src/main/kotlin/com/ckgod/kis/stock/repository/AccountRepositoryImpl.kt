package com.ckgod.kis.stock.repository

import com.ckgod.domain.model.AccountStatus
import com.ckgod.domain.repository.AccountRepository
import com.ckgod.kis.stock.api.KisApiService

class AccountRepositoryImpl(
    private val kisApiService: KisApiService,
): AccountRepository {
    override suspend fun getAccountBalance(): AccountStatus {
        val response = kisApiService.getAccountBalance()

        return response.toDomain()
    }
}