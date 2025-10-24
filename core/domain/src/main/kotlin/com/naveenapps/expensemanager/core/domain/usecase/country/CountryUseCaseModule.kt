package com.naveenapps.expensemanager.core.domain.usecase.country

import org.koin.dsl.module

val CountryUseCaseModule = module {
    single { GetCountiesUseCase(repository = get()) }
}

