package com.naveenapps.expensemanager.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.datastore.DateRangeDataStore
import com.naveenapps.expensemanager.core.datastore.ReminderTimeDataStore
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.datastore.ThemeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatastoreModule {

    private const val DATA_STORE_NAME = "expense_manager_app_data_store"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(DATA_STORE_NAME)

    @Provides
    @Singleton
    fun provideThemeDataStore(@ApplicationContext context: Context): ThemeDataStore {
        return ThemeDataStore(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideCurrencyDataStore(@ApplicationContext context: Context): CurrencyDataStore {
        return CurrencyDataStore(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideReminderTimeDataStore(@ApplicationContext context: Context): ReminderTimeDataStore {
        return ReminderTimeDataStore(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideDateRangeDataStore(@ApplicationContext context: Context): DateRangeDataStore {
        return DateRangeDataStore(context.dataStore)
    }
}