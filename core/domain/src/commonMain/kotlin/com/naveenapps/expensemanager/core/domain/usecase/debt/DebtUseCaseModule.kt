package com.naveenapps.expensemanager.core.domain.usecase.debt

import org.koin.dsl.module

val DebtUseCaseModule = module {
    single { GetDebtBalancesUseCase(get()) }
    single { GetDebtTotalsUseCase(get()) }
    single { SaveDebtEntryUseCase(get()) }
    single { SettleUpDebtUseCase(get()) }
    single { GetDebtActivityUseCase(get(), get(), get(), get()) }
}
