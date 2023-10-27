package com.nkuppan.expensemanager.data.repository

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.net.toUri
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.data.utils.toTransactionDate
import com.nkuppan.expensemanager.data.utils.toTransactionTimeOnly
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.ExportRepository
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import javax.inject.Inject


class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppCoroutineDispatchers
) : ExportRepository {
    override suspend fun createCsvFile(
        uri: String?,
        transactions: List<Transaction>
    ): Resource<Boolean> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                generateFile("sample.csv", uri) { output ->
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
                                    transaction.createdOn.toTransactionDate(),
                                    transaction.createdOn.toTransactionTimeOnly(),
                                    transaction.category.name,
                                    transaction.fromAccount.name,
                                    transaction.toAccount?.name ?: "",
                                    transaction.notes,
                                    transaction.amount.toTrimAmount(),
                                )
                            )
                        }
                    }
                    writer.writeAll(data)
                    writer.close()
                }
                Resource.Success(true)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Success(false)
        }

    override suspend fun createPdfFile(
        uri: String?,
        transactions: List<Transaction>
    ): Resource<Boolean> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                generateFile("sample.pdf", uri) {
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        100, 100, 1
                    ).create()
                    val page = document.startPage(pageInfo)
                    document.finishPage(page)
                    document.writeTo(it)
                    document.close()
                }
                Resource.Success(true)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Success(false)
        }

    private fun generateFile(fileName: String, uri: String?, write: (FileOutputStream) -> Unit) {
        try {
            if (uri != null) {
                context.contentResolver.openFileDescriptor(uri.toUri(), "w")?.use {
                    write.invoke(FileOutputStream(it.fileDescriptor))
                }
            } else {
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use(write)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

private fun Double.toTrimAmount(): String {
    return String.format(Locale.getDefault(), "%.1f", this)
}
