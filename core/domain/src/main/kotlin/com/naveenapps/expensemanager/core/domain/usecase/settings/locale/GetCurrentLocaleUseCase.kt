package com.naveenapps.expensemanager.core.domain.usecase.settings.locale

import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.repository.LocaleRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentLocaleUseCase(private val repository: LocaleRepository) {

    operator fun invoke(): Flow<AppLocale> {
        return repository.getSelectedLocale()
    }
}
