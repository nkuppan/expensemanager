package com.naveenapps.expensemanager.core.domain.usecase.transaction

import org.koin.dsl.module

val TransactionUseCaseModule = module {
    single {
        GetExportTransactionsUseCase(
            accountRepository = get(),
            categoryRepository = get(),
            transactionRepository = get(),
            getDateRangeByTypeUseCase = get()
        )
    }
    single {
        GetIncomeAmountUseCase(
            getTransactionWithFilterUseCase = get()
        )
    }
    single { AddTransactionUseCase(repository = get()) }
    single { DeleteTransactionUseCase(repository = get()) }
    single { FindTransactionByIdUseCase(repository = get()) }
    single {
        GetAmountStateUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getIncomeAmountUseCase = get(),
            getExpenseAmountUseCase = get(),
            dispatchers = get()
        )
    }
    single {
        GetAverageDataUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            getDateRangeUseCase = get(),
            dispatcher = get()
        )
    }
    single {
        GetChartDataUseCase(
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getDateRangeUseCase = get(),
            getTransactionGroupTypeUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            dispatcher = get()
        )
    }
    single { GetExpenseAmountUseCase(getTransactionWithFilterUseCase = get()) }
    single { GetTransactionByNameUseCase(repository = get()) }
    single {
        GetTransactionGroupByCategoryUseCase(
            getAllCategoryUseCase = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            getTransactionWithFilterUseCase = get(),
            appCoroutineDispatchers = get()
        )
    }
    single {
        GetTransactionWithFilterUseCase(
            accountRepository = get(),
            categoryRepository = get(),
            settingsRepository = get(),
            getDateRangeUseCase = get(),
            transactionRepository = get()
        )
    }
    single { UpdateTransactionUseCase(get()) }
}
