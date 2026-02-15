package com.naveenapps.expensemanager.core.domain.usecase.country

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.repository.CountryRepository

class GetCountriesUseCase(private val repository: CountryRepository) {
    suspend operator fun invoke(): List<Country> {
        return repository.readCountries()
    }
}
