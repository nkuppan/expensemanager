package com.naveenapps.expensemanager.feature.export

import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ExportFileType

data class ExportState(
    val isLoading: Boolean,
    val selectedDateRange: DateRangeType,
    val selectedDateRangeText: UiText,
    val selectedAccounts: List<AccountUiModel>,
    val fileType: ExportFileType,
    val accountCount: UiText,
    val isAllAccountSelected: Boolean,
    val showAccountSelection: Boolean,
)
