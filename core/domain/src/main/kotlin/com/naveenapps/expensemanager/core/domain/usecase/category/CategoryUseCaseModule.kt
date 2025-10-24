package com.naveenapps.expensemanager.core.domain.usecase.category

import org.koin.dsl.module

val CategoryUseCaseModule = module {
    single {
        AddCategoryUseCase(
            repository = get(),
            checkCategoryValidationUseCase = get()
        )
    }
    single { CheckCategoryValidationUseCase() }
    single {
        DeleteCategoryUseCase(
            repository = get(),
            checkCategoryValidationUseCase = get()
        )
    }
    single { FindCategoryByIdFlowUseCase(repository = get()) }
    single { FindCategoryByIdUseCase(repository = get()) }
    single { GetAllCategoryUseCase(repository = get()) }
    single { GetCategoryByNameUseCase(repository = get()) }
    single {
        UpdateCategoryUseCase(
            repository = get(),
            checkCategoryValidationUseCase = get()
        )
    }
}

