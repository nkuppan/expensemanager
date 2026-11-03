package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository
import kotlinx.coroutines.flow.Flow

class GetCurrencyApiProfileUseCase(
    private val repository: CurrencyApiRepository,
) {
    operator fun invoke(): Flow<CurrencyApiProfile?> = repository.getProfile()
}
