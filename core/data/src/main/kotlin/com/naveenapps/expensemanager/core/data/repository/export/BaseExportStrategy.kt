package com.naveenapps.expensemanager.core.data.repository.export

import android.content.Context
import androidx.core.net.toUri
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

internal fun Double.toTrimAmount(): String = String.format(Locale.getDefault(), "%.2f", this)

abstract class BaseExportStrategy(protected val context: Context) : ExportStrategy {

    protected fun getFileName(): String {
        val timestamp = System.currentTimeMillis()
        return when (getFileType()) {
            ExportFileType.CSV -> "expense_manager_$timestamp.csv"
            ExportFileType.PDF -> "expense_manager_$timestamp.pdf"
        }
    }

    /**
     * Writes the export to [uri] (a document the user picked) or, without one, to the app's
     * cache "exports" folder, which the FileProvider shares. Failures are thrown, not swallowed,
     * so callers report an error instead of "export successful" with nothing written.
     */
    protected fun generateFile(
        fileName: String,
        uri: String?,
        write: (FileOutputStream) -> Unit,
    ): ExportData = if (uri != null) {
        val descriptor = context.contentResolver.openFileDescriptor(uri.toUri(), "w")
            ?: throw IOException("Unable to open $uri for writing")
        descriptor.use { write.invoke(FileOutputStream(it.fileDescriptor)) }
        ExportData(uri, null)
    } else {
        val directory = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
        val file = File(directory, fileName)
        file.outputStream().use { write.invoke(it) }
        ExportData(null, file)
    }

    companion object {
        // Must match the <cache-path> in app/src/main/res/xml/provider_paths.xml.
        const val EXPORT_DIRECTORY = "exports"
    }
}

/**
 * Spreadsheet apps run a cell starting with = + - @ (or a tab/CR) as a formula, so free text
 * such as notes or names could execute when the CSV is opened. Prefixing a quote keeps it text.
 */
internal fun String.toCsvSafeText(): String = if (isNotEmpty() && first() in CSV_FORMULA_PREFIXES) "'$this" else this

private val CSV_FORMULA_PREFIXES = setOf('=', '+', '-', '@', '\t', '\r')
