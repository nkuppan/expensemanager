package com.naveenapps.expensemanager.core.domain.usecase.settings.locale

import com.naveenapps.expensemanager.core.repository.LocaleRepository

class ApplyLocaleUseCase(private val repository: LocaleRepository) {

    suspend operator fun invoke() {
        repository.applyLocale()
    }
}
