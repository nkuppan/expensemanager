package com.naveenapps.expensemanager.core.domain.usecase.transaction

import org.koin.dsl.module

val TransactionUseCaseModule = module {
    single {
        GetExportTransactionsUseCase(
            accountRepository = get(),
            categoryRepository = get(),
            transactionRepository = get(),
            getDateRangeByTypeUseCase = get(),
        )
    }
    single {
        GetIncomeAmountUseCase(
            getTransactionWithFilterUseCase = get(),
            getCurrencyUseCase = get(),
            convertAmountUseCase = get(),
        )
    }
    single { CheckTransactionValidationUseCase(accountRepository = get()) }
    single {
        AddTransactionUseCase(repository = get(), checkTransactionValidationUseCase = get())
    }
    single { GetMonthlyRecapUseCase(transactionRepository = get(), categoryRepository = get()) }
    single { GetLoggingStreakUseCase(transactionRepository = get(), feedbackRepository = get()) }
    single {
        GetSpendingInsightsUseCase(
            getTransactionWithFilterUseCase = get(),
            categoryRepository = get(),
            dispatchers = get(),
        )
    }
    single {
        GetPeriodComparisonUseCase(
            getTransactionWithFilterUseCase = get(),
            categoryRepository = get(),
            dispatchers = get(),
        )
    }
    single { DeleteTransactionUseCase(repository = get(), accountRepository = get()) }
    single { FindTransactionByIdUseCase(repository = get()) }
    single {
        GetAmountStateUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getIncomeAmountUseCase = get(),
            getExpenseAmountUseCase = get(),
            dispatchers = get(),
        )
    }
    single {
        GetAverageDataUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            getDateRangeUseCase = get(),
            convertAmountUseCase = get(),
            dispatcher = get(),
        )
    }
    single {
        GetChartDataUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getDateRangeUseCase = get(),
            getTransactionGroupTypeUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            convertAmountUseCase = get(),
            formatAmountInCurrencyUseCase = get(),
            dispatcher = get(),
        )
    }
    single {
        GetExpenseAmountUseCase(
            getTransactionWithFilterUseCase = get(),
            getCurrencyUseCase = get(),
            convertAmountUseCase = get(),
        )
    }
    single { GetTransactionByNameUseCase(repository = get()) }
    single {
        GetTransactionGroupByCategoryUseCase(
            getAllCategoryUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            convertAmountUseCase = get(),
            appCoroutineDispatchers = get(),
        )
    }
    single {
        GetTransactionWithFilterUseCase(
            accountRepository = get(),
            categoryRepository = get(),
            settingsRepository = get(),
            getDateRangeUseCase = get(),
            transactionRepository = get(),
            exchangeRateRepository = get(),
        )
    }
    single {
        UpdateTransactionUseCase(
            repository = get(),
            accountRepository = get(),
            checkTransactionValidationUseCase = get(),
        )
    }
}
