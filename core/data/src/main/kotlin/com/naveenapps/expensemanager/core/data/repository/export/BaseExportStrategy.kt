package com.naveenapps.expensemanager.core.data.repository.export

import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

internal fun Double.toTrimAmount(): String = String.format(Locale.getDefault(), "%.2f", this)

abstract class BaseExportStrategy(protected val context: Context) : ExportStrategy {

    protected fun getFileName(): String {
        val timestamp = System.currentTimeMillis()
        return when (getFileType()) {
            ExportFileType.CSV -> "expense_manager_${timestamp}.csv"
            ExportFileType.PDF -> "expense_manager_${timestamp}.pdf"
        }
    }

    protected fun generateFile(
        fileName: String,
        uri: String?,
        write: (FileOutputStream) -> Unit,
    ): ExportData {
        try {
            return if (uri != null) {
                context.contentResolver.openFileDescriptor(uri.toUri(), "w")?.use {
                    write.invoke(FileOutputStream(it.fileDescriptor))
                }
                ExportData(uri, null)
            } else {
                val directory = File(Environment.getExternalStorageDirectory(), "Expense Manager")
                if (!directory.exists()) directory.mkdir()
                val file = File(directory, fileName)
                file.outputStream().use { write.invoke(it) }
                ExportData(null, file)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ExportData(null, null)
    }
}
