package com.naveenapps.expensemanager.core.domain.usecase.settings.locale

import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.repository.LocaleRepository

class GetLocalesUseCase(private val repository: LocaleRepository) {

    operator fun invoke(): List<AppLocale> {
        return repository.getLocales()
    }
}
