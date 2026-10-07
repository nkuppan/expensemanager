package com.naveenapps.expensemanager.core.domain.usecase.recurring

import org.koin.dsl.module

val RecurringUseCaseModule = module {
    single { CreateRecurringTransactionUseCase(repository = get()) }
    single { ProcessRecurringTransactionsUseCase(repository = get(), addTransactionUseCase = get()) }
    single {
        GetRecurringTransactionsUseCase(
            repository = get(),
            accountRepository = get(),
            categoryRepository = get(),
        )
    }
    single { DeleteRecurringTransactionUseCase(repository = get()) }
}
