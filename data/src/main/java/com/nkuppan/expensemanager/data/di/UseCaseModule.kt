package com.nkuppan.expensemanager.data.di

import com.nkuppan.expensemanager.data.repository.*
import com.nkuppan.expensemanager.data.usecase.account.*
import com.nkuppan.expensemanager.data.usecase.category.*
import com.nkuppan.expensemanager.data.usecase.settings.GetReminderStatusUseCase
import com.nkuppan.expensemanager.data.usecase.settings.UpdateReminderStatusUseCase
import com.nkuppan.expensemanager.data.usecase.settings.account.GetSelectedAccountUseCase
import com.nkuppan.expensemanager.data.usecase.settings.account.UpdateSelectedAccountUseCase
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetAllCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.settings.currency.SaveCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.settings.filter.*
import com.nkuppan.expensemanager.data.usecase.settings.theme.ApplyThemeUseCase
import com.nkuppan.expensemanager.data.usecase.settings.theme.GetThemeUseCase
import com.nkuppan.expensemanager.data.usecase.settings.theme.SaveThemeUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAllAccountsUseCase(repository: AccountRepository): GetAllAccountsUseCase {
        return GetAllAccountsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAccountsByNameUseCase(repository: AccountRepository): GetAccountsByNameUseCase {
        return GetAccountsByNameUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAccountByIdUseCase(repository: AccountRepository): GetAccountByIdUseCase {
        return GetAccountByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddAccountUseCase(
        repository: AccountRepository, checkAccountValidationUseCase: CheckAccountValidationUseCase
    ): AddAccountUseCase {
        return AddAccountUseCase(repository, checkAccountValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateAccountUseCase(
        repository: AccountRepository, checkAccountValidationUseCase: CheckAccountValidationUseCase
    ): UpdateAccountUseCase {
        return UpdateAccountUseCase(repository, checkAccountValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideDeleteAccountUseCase(
        repository: AccountRepository, checkAccountValidationUseCase: CheckAccountValidationUseCase
    ): DeleteAccountUseCase {
        return DeleteAccountUseCase(repository, checkAccountValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideGetAllCategoriesUseCase(repository: CategoryRepository): GetAllCategoryUseCase {
        return GetAllCategoryUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCategoryByNameUseCase(repository: CategoryRepository): GetCategoryByNameUseCase {
        return GetCategoryByNameUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(
        repository: CategoryRepository,
        checkCategoryValidationUseCase: CheckCategoryValidationUseCase
    ): AddCategoryUseCase {
        return AddCategoryUseCase(repository, checkCategoryValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateCategoryUseCase(
        repository: CategoryRepository,
        checkCategoryValidationUseCase: CheckCategoryValidationUseCase
    ): UpdateCategoryUseCase {
        return UpdateCategoryUseCase(repository, checkCategoryValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(
        repository: CategoryRepository,
        checkCategoryValidationUseCase: CheckCategoryValidationUseCase
    ): DeleteCategoryUseCase {
        return DeleteCategoryUseCase(repository, checkCategoryValidationUseCase)
    }

    @Provides
    @Singleton
    fun provideGetThisSpecificCategoryUseCase(repository: CategoryRepository): FindCategoryByIdUseCase {
        return FindCategoryByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetGroupedTransactionUseCase(
        transactionRepository: TransactionRepository
    ): GetTransactionGroupByMonthUseCase {
        return GetTransactionGroupByMonthUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransactionGroupByCategoryUseCase(
        getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
    ): GetTransactionGroupByCategoryUseCase {
        return GetTransactionGroupByCategoryUseCase(getTransactionWithFilterUseCase)
    }

    @Provides
    @Singleton
    fun provideGetTransactionWithFilterUseCase(
        settingsRepository: SettingsRepository, transactionRepository: TransactionRepository
    ): GetTransactionWithFilterUseCase {
        return GetTransactionWithFilterUseCase(settingsRepository, transactionRepository)
    }

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(transactionRepository: TransactionRepository): AddTransactionUseCase {
        return AddTransactionUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(transactionRepository: TransactionRepository): DeleteTransactionUseCase {
        return DeleteTransactionUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransactionByIdUseCase(transactionRepository: TransactionRepository): GetTransactionByIdUseCase {
        return GetTransactionByIdUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransactionByNameUseCase(transactionRepository: TransactionRepository): GetTransactionByNameUseCase {
        return GetTransactionByNameUseCase(transactionRepository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentSymbolUseCase(repository: CurrencyRepository): GetCurrencyUseCase {
        return GetCurrencyUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveCurrentSymbolUseCase(repository: CurrencyRepository): SaveCurrencyUseCase {
        return SaveCurrencyUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAllCurrencyUseCase(repository: CurrencyRepository): GetAllCurrencyUseCase {
        return GetAllCurrencyUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetThemeUseCase(themeRepository: ThemeRepository): GetThemeUseCase {
        return GetThemeUseCase(themeRepository)
    }

    @Provides
    @Singleton
    fun provideSaveThemeUseCase(themeRepository: ThemeRepository): SaveThemeUseCase {
        return SaveThemeUseCase(themeRepository)
    }

    @Provides
    @Singleton
    fun provideApplyThemeUseCase(themeRepository: ThemeRepository): ApplyThemeUseCase {
        return ApplyThemeUseCase(themeRepository)
    }

    @Provides
    @Singleton
    fun provideGetReminderStatusUseCase(
        settingsRepository: SettingsRepository
    ): GetReminderStatusUseCase {
        return GetReminderStatusUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateReminderStatusUseCase(
        settingsRepository: SettingsRepository
    ): UpdateReminderStatusUseCase {
        return UpdateReminderStatusUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideGetExpenseAmountUseCase(
        getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
    ): GetExpenseAmountUseCase {
        return GetExpenseAmountUseCase(getTransactionWithFilterUseCase)
    }

    @Provides
    @Singleton
    fun provideGetIncomeAmountUseCase(
        getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
    ): GetIncomeAmountUseCase {
        return GetIncomeAmountUseCase(getTransactionWithFilterUseCase)
    }

    @Provides
    @Singleton
    fun provideGetPreviousDaysTransactionWithFilterUseCase(
        transactionRepository: TransactionRepository, settingsRepository: SettingsRepository
    ): GetPreviousDaysTransactionWithFilterUseCase {
        return GetPreviousDaysTransactionWithFilterUseCase(
            transactionRepository, settingsRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetFilterRange(settingsRepository: SettingsRepository): GetFilterTypeTextUseCase {
        return GetFilterTypeTextUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideGetFilterType(settingsRepository: SettingsRepository): GetFilterTypeUseCase {
        return GetFilterTypeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSaveFilterTypeUseCase(settingsRepository: SettingsRepository): SaveFilterTypeUseCase {
        return SaveFilterTypeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideGetFilterValueUseCase(settingsRepository: SettingsRepository): GetFilterRangeUseCase {
        return GetFilterRangeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideSetCustomFilterRangeUseCase(settingsRepository: SettingsRepository): SetCustomFilterRangeUseCase {
        return SetCustomFilterRangeUseCase(settingsRepository)
    }

    @Provides
    @Singleton
    fun provideGetSelectedAccountUseCase(
        settingsRepository: SettingsRepository, accountRepository: AccountRepository
    ): GetSelectedAccountUseCase {
        return GetSelectedAccountUseCase(settingsRepository, accountRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateSelectedAccountUseCase(
        settingsRepository: SettingsRepository
    ): UpdateSelectedAccountUseCase {
        return UpdateSelectedAccountUseCase(settingsRepository)
    }
}