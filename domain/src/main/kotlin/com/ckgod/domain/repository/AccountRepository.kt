package com.ckgod.domain.repository

import com.ckgod.domain.model.AccountStatus

interface AccountRepository {
    suspend fun getAccountBalance(): AccountStatus
}