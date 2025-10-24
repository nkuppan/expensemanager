package com.naveenapps.expensemanager.core.domain.usecase.settings.filter

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.GetSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.UpdateSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.GetSelectedCategoriesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.UpdateSelectedCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetAllDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeByTypeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeBackwardUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeForwardUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.SaveDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.SetDateRangesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.GetSelectedTransactionTypesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.UpdateSelectedTransactionTypesUseCase
import org.koin.dsl.module

val FilterUseCaseModule = module {
    // Category use cases
    single { UpdateSelectedCategoryUseCase(get()) }
    single {
        GetSelectedCategoriesUseCase(
            settingsRepository = get(),
            findCategoryByIdUseCase = get()
        )
    }

    // Account use cases
    single {
        GetSelectedAccountUseCase(
            settingsRepository = get(),
            findAccountByIdUseCase = get()
        )
    }
    single { UpdateSelectedAccountUseCase(get()) }

    // Date range use cases
    single { GetAllDateRangeUseCase(get()) }
    single { GetDateRangeByTypeUseCase(get()) }
    single {
        GetDateRangeUseCase(
            getDateRangeByTypeUseCase = get(),
            dateRangeFilterRepository = get()
        )
    }
    single { GetTransactionGroupTypeUseCase(get()) }
    single { MoveDateRangeBackwardUseCase(get()) }
    single { MoveDateRangeForwardUseCase(get()) }
    single {
        SaveDateRangeUseCase(
            dateRangeFilterRepository = get(),
            setDateRangesUseCase = get()
        )
    }
    single { SetDateRangesUseCase(get()) }

    // Transaction type use cases
    single { GetSelectedTransactionTypesUseCase(get()) }
    single { UpdateSelectedTransactionTypesUseCase(get()) }
}