package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppCoroutineDispatchers
) : ExportRepository {
    override suspend fun createCsvFile(
        uri: String?,
        transactions: List<Transaction>
    ): Resource<String?> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                val fileUri = generateFile(getFileName(ExportFileType.CSV), uri) { output ->
                    val writer = CSVWriter(output.writer())
                    val data = mutableListOf<Array<String>>()
                    data.add(
                        arrayOf(
                            context.getString(R.string.date),
                            context.getString(R.string.time),
                            context.getString(R.string.category),
                            context.getString(R.string.from_account),
                            context.getString(R.string.to_account),
                            context.getString(R.string.notes),
                            context.getString(R.string.amount),
                        )
                    )
                    if (transactions.isNotEmpty()) {
                        transactions.forEach { transaction ->
                            data.add(
                                arrayOf(
                                    transaction.createdOn.toCompleteDate(),
                                    transaction.createdOn.toTimeAndMinutes(),
                                    transaction.category.name,
                                    transaction.fromAccount.name,
                                    transaction.toAccount?.name ?: "",
                                    transaction.notes,
                                    transaction.amount.amount.toTrimAmount(),
                                )
                            )
                        }
                    }
                    writer.writeAll(data)
                    writer.close()
                }
                Resource.Success(fileUri)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Error(java.lang.Exception())
        }

    override suspend fun createPdfFile(
        uri: String?,
        transactions: List<Transaction>
    ): Resource<String?> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                val fileUri = generateFile(getFileName(ExportFileType.PDF), uri) { output ->
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        100, 100, 1
                    ).create()
                    val page = document.startPage(pageInfo)
                    document.finishPage(page)
                    document.writeTo(output)
                    document.close()
                }
                Resource.Success(fileUri)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Error(java.lang.Exception())
        }

    private fun generateFile(
        fileName: String,
        uri: String?,
        write: (FileOutputStream) -> Unit
    ): String? {
        try {
            return if (uri != null) {
                context.contentResolver.openFileDescriptor(uri.toUri(), "w")?.use {
                    write.invoke(FileOutputStream(it.fileDescriptor))
                }
                uri
            } else {
                val directory = File(Environment.getExternalStorageDirectory(), "Expense Manager")
                if (directory.exists().not()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                file.outputStream().use {
                    write.invoke(it)
                }
                directory.toUri().toString()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFileName(exportFileType: ExportFileType): String {
        return when (exportFileType) {
            ExportFileType.CSV -> {
                "export_file_${Date().time}$CSV_FILE_EXTENSION"
            }

            ExportFileType.PDF -> {
                "export_file_${Date().time}$PDF_FILE_EXTENSION"
            }
        }
    }

    companion object {
        private const val CSV_FILE_EXTENSION = ".csv"
        private const val PDF_FILE_EXTENSION = ".pdf"
    }
}

private fun Double.toTrimAmount(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}
