package com.naveenapps.expensemanager.core.domain.usecase.settings

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.export.ExportFileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.GetReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.SaveReminderTimeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.ApplyThemeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetThemesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.SaveThemeUseCase
import org.koin.dsl.module

val SettingsUseCaseModule = module {
    //Currency
    single { GetCurrencyUseCase(get()) }
    single { GetDefaultCurrencyUseCase(get()) }
    single { GetFormattedAmountUseCase(get()) }
    single { SaveCurrencyUseCase(get()) }

    //Export
    single {
        ExportFileUseCase(
            exportRepository = get(),
            getExportTransactionsUseCase = get()
        )
    }

    //Onboarding
    single { GetOnboardingStatusUseCase(get()) }
    single { GetPreloadStatusUseCase(get()) }
    single { SetOnboardingStatusUseCase(get()) }
    single { SetPreloadStatusUseCase(get()) }

    //Reminder
    single { GetReminderStatusUseCase(get()) }
    single { GetReminderTimeUseCase(get()) }
    single { SaveReminderTimeUseCase(get()) }
    single { UpdateReminderStatusUseCase(get()) }

    //Theme
    single { GetCurrentThemeUseCase(get()) }
    single { GetThemesUseCase(get()) }
    single { SaveThemeUseCase(get()) }
    single { ApplyThemeUseCase(get()) }

}
