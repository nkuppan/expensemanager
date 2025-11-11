package com.naveenapps.expensemanager.core.settings.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.naveenapps.expensemanager.core.settings.data.datastore.NumberFormatSettingsDatastore
import com.naveenapps.expensemanager.core.settings.data.repository.NumberFormatRepositoryImpl
import com.naveenapps.expensemanager.core.settings.data.repository.NumberFormatSettingRepositoryImpl
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatSettingRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATA_STORE_NAME = "expense_manager_settings"

private val Context.expenseManagerSettingsDataStore by preferencesDataStore(DATA_STORE_NAME)

val CoreSettingsModule = module {
    single { androidContext().expenseManagerSettingsDataStore }
    single {
        NumberFormatSettingsDatastore(dataStore = get())
    }
    single<NumberFormatSettingRepository> {
        NumberFormatSettingRepositoryImpl(
            numberFormatSettingsDatastore = get()
        )
    }
    single<NumberFormatSettingRepository> {
        NumberFormatSettingRepositoryImpl(
            numberFormatSettingsDatastore = get()
        )
    }
    single<NumberFormatRepository> {
        NumberFormatRepositoryImpl(
            numberFormatSettingRepository = get()
        )
    }
}