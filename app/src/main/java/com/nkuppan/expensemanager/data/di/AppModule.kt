package com.nkuppan.expensemanager.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nkuppan.expensemanager.data.datastore.CurrencyDataStore
import com.nkuppan.expensemanager.data.datastore.ReminderTimeDataStore
import com.nkuppan.expensemanager.data.datastore.SettingsDataStore
import com.nkuppan.expensemanager.data.datastore.ThemeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

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
}