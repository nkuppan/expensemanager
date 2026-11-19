package com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi

import com.naveenapps.expensemanager.core.model.CurrencyApiProfile
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CurrencyApiRepository

class SaveCurrencyApiProfileUseCase(
    private val repository: CurrencyApiRepository,
) {
    suspend operator fun invoke(profile: CurrencyApiProfile): Resource<Boolean> {
        if (profile.baseUrl.isBlank()) {
            return Resource.Error(Exception("Please provide a valid API base URL"))
        }
        if (profile.preset.defaultBaseUrl.isBlank() && !profile.baseUrl.startsWith("http")) {
            return Resource.Error(Exception("Base URL must start with http:// or https://"))
        }
        repository.saveProfile(profile)
        return Resource.Success(true)
    }
}
