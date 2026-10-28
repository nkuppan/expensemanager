package com.naveenapps.expensemanager.feature.budget

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.model.BudgetTitle
import java.text.SimpleDateFormat

/** The budget's name as shown on screen, translated and with the month in the user's locale. */
@Composable
fun BudgetTitle.text(): String = when (this) {
    is BudgetTitle.Custom -> name

    BudgetTitle.ThisMonth -> stringResource(R.string.budget_title_this_month)

    BudgetTitle.ThisYear -> stringResource(R.string.budget_title_this_year)

    is BudgetTitle.Month -> {
        val locale = LocalConfiguration.current.locales[0]
        val format = remember(locale) {
            SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMMMyyyy"), locale)
        }
        stringResource(R.string.budget_title_period, format.format(date))
    }

    is BudgetTitle.Period -> stringResource(R.string.budget_title_period, label)
}
