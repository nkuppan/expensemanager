package com.nkuppan.expensemanager.data.repository

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.ExportRepository
import com.opencsv.CSVWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject


class ExportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppCoroutineDispatchers
) : ExportRepository {
    override suspend fun createCsvFile(): Resource<Boolean> = withContext(dispatchers.io) {
        val file = generateFile("sample.csv")

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

    override suspend fun createPdfFile(): Resource<Boolean> = withContext(dispatchers.io) {
        val fileOutputStream = generateFile("sample.pdf")
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

    private fun generateFile(fileName: String): FileOutputStream? {
        return context.openFileOutput(fileName, Context.MODE_PRIVATE)
    }
}