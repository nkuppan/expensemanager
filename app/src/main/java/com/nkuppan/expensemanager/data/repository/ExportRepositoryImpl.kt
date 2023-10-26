package com.nkuppan.expensemanager.data.repository

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.net.toUri
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.ExportRepository
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppCoroutineDispatchers
) : ExportRepository {
    override suspend fun createCsvFile(uri: String?): Resource<Boolean> =
        withContext(dispatchers.io) {
            val file = generateFile("sample.csv", uri)

            val data = kotlin.runCatching {
                val writer = CSVWriter(file?.writer())
                val data = mutableListOf<Array<String>>()
                data.add(arrayOf("Country", "Capital"))
                data.add(arrayOf("India", "New Delhi"))
                data.add(arrayOf("United States", "Washington D.C"))
                data.add(arrayOf("Germany", "Berlin"))
                writer.writeAll(data)
                writer.close()
                Resource.Success(true)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Success(false)

            return@withContext data
        }

    override suspend fun createPdfFile(uri: String?): Resource<Boolean> =
        withContext(dispatchers.io) {
            val fileOutputStream = generateFile("sample.pdf", uri)
            kotlin.runCatching {
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(
                    100, 100, 1
                ).create()
                val page = document.startPage(pageInfo)
                document.finishPage(page)
                document.writeTo(fileOutputStream)
                document.close()
                Resource.Success(true)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Success(false)
        }

    private fun generateFile(fileName: String, uri: String?): FileOutputStream? {
        return try {
            if (uri != null) {
                context.contentResolver.openFileDescriptor(uri.toUri(), "w")?.let {
                    return@let FileOutputStream(it.fileDescriptor)
                }
            } else {
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}