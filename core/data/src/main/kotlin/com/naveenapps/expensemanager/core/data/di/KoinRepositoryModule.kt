package com.naveenapps.expensemanager.core.data.di

import com.naveenapps.expensemanager.core.data.repository.AccountRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AnalyticsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.BudgetRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategoryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CountryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CurrencyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DevicePropertyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ExportRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.FeedbackRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.FirebaseSettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.JsonConverterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.SettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ShareRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ThemeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.TransactionRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.VersionCheckerRepositoryImpl
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import com.naveenapps.expensemanager.core.repository.ExportRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.FirebaseSettingsRepository
import com.naveenapps.expensemanager.core.repository.JsonConverterRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val RepositoryModule = module {
    single<ThemeRepository> {
        ThemeRepositoryImpl(
            dataStore = get(),
            versionCheckerRepository = get(),
            dispatchers = get()
        )
    }
    single<AnalyticsRepository> {
        AnalyticsRepositoryImpl(
            firebaseAnalytics = get(),
            devicePropertyRepository = get()
        )
    }
    single<JsonConverterRepository> {
        JsonConverterRepositoryImpl(
            gson = get(),
            appCoroutineDispatchers = get()
        )
    }
    single<CountryRepository> {
        CountryRepositoryImpl(
            context = androidContext(),
            jsonConverterRepository = get(),
            dispatchers = get()
        )
    }
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            dispatchers = get(),
            dataStore = get(),
            numberFormatRepository = get(),
        )
    }
    single<DevicePropertyRepository> { DevicePropertyRepositoryImpl() }
    single<FeedbackRepository> { FeedbackRepositoryImpl(feedbackDataStore = get()) }
    single<FirebaseSettingsRepository> { FirebaseSettingsRepositoryImpl(firebaseRemoteConfig = get()) }
    single<VersionCheckerRepository> { VersionCheckerRepositoryImpl() }

    single<AccountRepository> {
        AccountRepositoryImpl(
            accountDao = get(),
            dispatchers = get()
        )
    }
    single<BudgetRepository> {
        BudgetRepositoryImpl(
            budgetDao = get(),
            dispatchers = get()
        )
    }
    single<DateRangeFilterRepository> {
        DateRangeFilterRepositoryImpl(
            dataStore = get(),
            dispatcher = get()
        )
    }
    single<ExportRepository> {
        ExportRepositoryImpl(
            context = androidContext(),
            dispatchers = get()
        )
    }
    single<ReminderTimeRepository> {
        ReminderTimeRepositoryImpl(
            dataStore = get(),
            dispatchers = get()
        )
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            dataStore = get(),
            dispatchers = get()
        )
    }
    single<ShareRepository> {
        ShareRepositoryImpl(
            context = androidContext(),
            firebaseSettingsRepository = get()
        )
    }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            accountDao = get(),
            categoryDao = get(),
            dispatchers = get()
        )
    }
    single<CategoryRepository> {
        CategoryRepositoryImpl(
            categoryDao = get(),
            dispatchers = get()
        )
    }
}
