package com.naveenapps.expensemanager.feature.export

import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.model.ExportData

sealed class ExportEvent {

    data class Error(val message: UiText) : ExportEvent()

    data object CreateFile : ExportEvent()

    data class FileExported(
        val message: UiText,
        val exportData: ExportData
    ) : ExportEvent()
}