package com.naveenapps.expensemanager.core.domain.usecase.settings.locale

import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.LocaleRepository

class SaveLocaleUseCase(private val repository: LocaleRepository) {

    suspend operator fun invoke(locale: AppLocale): Resource<Boolean> {
        return Resource.Success(repository.saveLocale(locale))
    }
}
