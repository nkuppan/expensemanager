package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Country


interface CountryRepository {

    suspend fun readCountries(): List<Country>
}