package com.naveenapps.expensemanager.feature.budget

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.model.BudgetPeriod

/** "Monthly" / "Annual", as shown on the period switch. */
@Composable
fun BudgetPeriod.label(): String = stringResource(
    id = when (this) {
        BudgetPeriod.MONTHLY -> R.string.monthly
        BudgetPeriod.YEARLY -> R.string.annual
    },
)
