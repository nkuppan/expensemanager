package com.naveenapps.expensemanager.core.settings.data.repository

import com.naveenapps.expensemanager.core.settings.data.datastore.NumberFormatSettingsDatastore
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import kotlinx.coroutines.flow.Flow

class NumberFormatSettingRepositoryImpl(
    private val numberFormatSettingsDatastore: NumberFormatSettingsDatastore
) : NumberFormatSettingRepository {

    override fun getNumberFormatType(): Flow<NumberFormatType> {
        return numberFormatSettingsDatastore.getNumberFormatType()
    }

    override suspend fun saveNumberFormatType(numberFormatType: NumberFormatType) {
        numberFormatSettingsDatastore.setNumberFormatType(numberFormatType)
    }
}