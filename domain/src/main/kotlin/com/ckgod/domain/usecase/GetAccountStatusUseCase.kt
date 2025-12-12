package com.ckgod.domain.usecase

import com.ckgod.domain.model.AccountStatus
import com.ckgod.domain.repository.AccountRepository

class GetAccountStatusUseCase(
    val realRepository: AccountRepository,
    val mockRepository: AccountRepository
) {
    suspend operator fun invoke(isRealMode: Boolean): AccountStatus {
        val repository = if (isRealMode) realRepository else mockRepository
        return repository.getAccountBalance()
    }
}