package com.naveenapps.expensemanager.feature.export

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.ExportFileType

sealed class ExportAction {

    data object ClosePage : ExportAction()

    data object OpenAccountSelection : ExportAction()

    data object CloseAccountSelection : ExportAction()

    data class StartExport(val uri: String?) : ExportAction()

    data class ChangeFileType(val fileType: ExportFileType) : ExportAction()

    data class AccountSelection(
        val accounts: List<AccountUiModel>,
        val isAllAccountSelected: Boolean
    ) : ExportAction()
}