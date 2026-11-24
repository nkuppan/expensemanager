package com.naveenapps.expensemanager.core.model

data class ExportData(
    val uri: String?,
    // Absolute path of the file written to the app's cache when there was no picked [uri].
    // A plain path rather than java.io.File keeps this model platform-neutral.
    val filePath: String?,
)
