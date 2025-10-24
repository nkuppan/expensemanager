package com.naveenapps.expensemanager.core.domain.usecase.account

import org.koin.dsl.module

val AccountUseCaseModule = module {
    single { AddAccountUseCase(get(), checkAccountValidationUseCase = get()) }
    single { CheckAccountValidationUseCase() }
    single { DeleteAccountUseCase(repository = get(), checkAccountValidationUseCase = get()) }
    single { FindAccountByIdUseCase(get()) }
    single { GetAllAccountsUseCase(get()) }
    single { UpdateAccountUseCase(get(), checkAccountValidationUseCase = get()) }
    single { UpdateAllAccountUseCase(get(), checkAccountValidationUseCase = get()) }
}

