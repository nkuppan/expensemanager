package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.ExportRepository
import com.opencsv.CSVWriter
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.Locale

class ExportRepositoryImpl(
    private val context: Context,
    private val dispatchers: AppCoroutineDispatchers,
) : ExportRepository {
    override suspend fun createCsvFile(
        uri: String?,
        transactions: List<Transaction>,
    ): Resource<ExportData> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                val fileUri = generateFile(getFileName(ExportFileType.CSV), uri) { output ->
                    val writer = CSVWriter(output.writer())
                    val data = mutableListOf<Array<String>>()

                    // ── Summary block ──────────────────────────────────
                    val totalIncome = transactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount.amount }
                    val totalExpense = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount.amount }
                    val now = Date()
                    data.add(arrayOf(
                        context.getString(R.string.exported_on),
                        "${now.toCompleteDate()} ${now.toTimeAndMinutes()}",
                    ))
                    data.add(arrayOf(
                        context.getString(R.string.total_transactions),
                        transactions.size.toString(),
                    ))
                    data.add(arrayOf(
                        context.getString(R.string.total_income),
                        totalIncome.toTrimAmount(),
                    ))
                    data.add(arrayOf(
                        context.getString(R.string.total_expense),
                        totalExpense.toTrimAmount(),
                    ))
                    data.add(arrayOf(
                        context.getString(R.string.net_balance),
                        (totalIncome - totalExpense).toTrimAmount(),
                    ))
                    data.add(arrayOf()) // blank separator row

                    // ── Column headers ─────────────────────────────────
                    data.add(arrayOf(
                        context.getString(R.string.date),
                        context.getString(R.string.time),
                        context.getString(R.string.transaction_type),
                        context.getString(R.string.category),
                        context.getString(R.string.category_type),
                        context.getString(R.string.from_account),
                        context.getString(R.string.account_type),
                        context.getString(R.string.to_account),
                        context.getString(R.string.amount),
                        context.getString(R.string.notes),
                    ))

                    // ── Data rows ──────────────────────────────────────
                    transactions.forEach { transaction ->
                        val signedAmount = when (transaction.type) {
                            TransactionType.EXPENSE -> -transaction.amount.amount
                            else -> transaction.amount.amount
                        }
                        data.add(arrayOf(
                            transaction.createdOn.toCompleteDate(),
                            transaction.createdOn.toTimeAndMinutes(),
                            transaction.type.toCapitalize(),
                            transaction.category.name,
                            transaction.category.type.toCapitalize(),
                            transaction.fromAccount.name,
                            transaction.fromAccount.type.toCapitalize(),
                            transaction.toAccount?.name ?: "",
                            signedAmount.toTrimAmount(),
                            transaction.notes,
                        ))
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
        transactions: List<Transaction>,
    ): Resource<ExportData> =
        withContext(dispatchers.io) {
            kotlin.runCatching {
                val fileUri = generateFile(getFileName(ExportFileType.PDF), uri) { output ->
                    buildPdf(transactions).also { doc ->
                        doc.writeTo(output)
                        doc.close()
                    }
                }
                Resource.Success(fileUri)
            }.onFailure {
                Log.i("error", it.localizedMessage ?: "")
                Resource.Error(Exception(it))
            }.getOrNull() ?: Resource.Error(java.lang.Exception())
        }

    private fun buildPdf(transactions: List<Transaction>): PdfDocument {
        // A4 landscape: 842 x 595 pts
        val pageWidth = 842
        val pageHeight = 595
        val margin = 32f
        val contentWidth = pageWidth - margin * 2  // 778

        // Column widths — last column (Notes) fills remaining space
        val fixedWidths = floatArrayOf(85f, 38f, 55f, 75f, 55f, 75f, 55f, 65f, 55f)
        val colWidths = fixedWidths + floatArrayOf(contentWidth - fixedWidths.sum())

        // ── Paints ────────────────────────────────────────────────────
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f; isFakeBoldText = true; color = Color.BLACK
        }
        val summaryLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; isFakeBoldText = true; color = Color.rgb(80, 80, 80)
        }
        val summaryValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = Color.BLACK
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 40, 40)
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; isFakeBoldText = true; color = Color.WHITE
        }
        val rowAltBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(246, 246, 246)
        }
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.BLACK
        }
        val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.rgb(27, 128, 62)
        }
        val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8f; color = Color.rgb(185, 28, 28)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 220, 220); strokeWidth = 0.5f
        }

        val colHeaders = arrayOf(
            context.getString(R.string.date),
            context.getString(R.string.time),
            context.getString(R.string.transaction_type),
            context.getString(R.string.category),
            context.getString(R.string.category_type),
            context.getString(R.string.from_account),
            context.getString(R.string.account_type),
            context.getString(R.string.to_account),
            context.getString(R.string.amount),
            context.getString(R.string.notes),
        )

        val rowHeight = 18f
        val headerRowHeight = 22f

        // ── Helpers ────────────────────────────────────────────────────
        fun clipText(text: String, maxWidth: Float, paint: Paint): String {
            if (paint.measureText(text) <= maxWidth) return text
            var result = text
            while (result.isNotEmpty() && paint.measureText("$result…") > maxWidth) {
                result = result.dropLast(1)
            }
            return if (result.isEmpty()) "" else "$result…"
        }

        fun drawColumnHeaders(c: Canvas, yTop: Float) {
            c.drawRect(margin, yTop, margin + contentWidth, yTop + headerRowHeight, headerBgPaint)
            var x = margin + 4f
            colHeaders.forEachIndexed { i, label ->
                c.drawText(clipText(label, colWidths[i] - 6f, headerTextPaint), x, yTop + 15f, headerTextPaint)
                x += colWidths[i]
            }
        }

        fun drawDataRow(c: Canvas, transaction: Transaction, rowIndex: Int, yTop: Float) {
            if (rowIndex % 2 == 1) {
                c.drawRect(margin, yTop, margin + contentWidth, yTop + rowHeight, rowAltBgPaint)
            }
            val signedAmount = if (transaction.type == TransactionType.EXPENSE) {
                -transaction.amount.amount
            } else {
                transaction.amount.amount
            }
            val cells = arrayOf(
                transaction.createdOn.toCompleteDate(),
                transaction.createdOn.toTimeAndMinutes(),
                transaction.type.toCapitalize(),
                transaction.category.name,
                transaction.category.type.toCapitalize(),
                transaction.fromAccount.name,
                transaction.fromAccount.type.toCapitalize(),
                transaction.toAccount?.name ?: "",
                signedAmount.toTrimAmount(),
                transaction.notes,
            )
            var x = margin + 4f
            cells.forEachIndexed { i, cell ->
                val paint = if (i == 8) {
                    if (signedAmount < 0) expensePaint else incomePaint
                } else {
                    cellTextPaint
                }
                c.drawText(clipText(cell, colWidths[i] - 6f, paint), x, yTop + rowHeight - 5f, paint)
                x += colWidths[i]
            }
            c.drawLine(margin, yTop + rowHeight, margin + contentWidth, yTop + rowHeight, dividerPaint)
        }

        // ── Build pages ────────────────────────────────────────────────
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.amount }
        val now = Date()

        val document = PdfDocument()
        var pageNum = 1

        fun newPage(): Pair<PdfDocument.Page, Canvas> {
            val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum++).create()
            val p = document.startPage(info)
            return p to p.canvas
        }

        var (page, canvas) = newPage()
        var y = margin

        // Title
        canvas.drawText("Expense Manager", margin, y + 14f, titlePaint)
        y += 26f

        // Summary block
        fun summaryRow(label: String, value: String) {
            canvas.drawText(label, margin, y, summaryLabelPaint)
            canvas.drawText(value, margin + 170f, y, summaryValuePaint)
            y += 14f
        }
        summaryRow(context.getString(R.string.exported_on), "${now.toCompleteDate()} ${now.toTimeAndMinutes()}")
        summaryRow(context.getString(R.string.total_transactions), transactions.size.toString())
        summaryRow(context.getString(R.string.total_income), totalIncome.toTrimAmount())
        summaryRow(context.getString(R.string.total_expense), totalExpense.toTrimAmount())
        summaryRow(context.getString(R.string.net_balance), (totalIncome - totalExpense).toTrimAmount())

        // Divider below summary
        y += 4f
        canvas.drawLine(margin, y, margin + contentWidth, y, dividerPaint)
        y += 10f

        // Column headers on first page
        drawColumnHeaders(canvas, y)
        y += headerRowHeight

        // Data rows — paginate automatically
        transactions.forEachIndexed { index, transaction ->
            if (y + rowHeight > pageHeight - margin) {
                document.finishPage(page)
                val next = newPage()
                page = next.first
                canvas = next.second
                y = margin
                drawColumnHeaders(canvas, y)
                y += headerRowHeight
            }
            drawDataRow(canvas, transaction, index, y)
            y += rowHeight
        }

        document.finishPage(page)
        return document
    }

    private fun generateFile(
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
                if (!directory.exists()) {
                    directory.mkdir()
                }
                val file = File(directory, fileName)
                file.outputStream().use {
                    write.invoke(it)
                }
                ExportData(null, file)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ExportData(null, null)
    }

    private fun getFileName(exportFileType: ExportFileType): String {
        val timestamp = System.currentTimeMillis()
        return when (exportFileType) {
            ExportFileType.CSV -> "expense_manager_${timestamp}$CSV_FILE_EXTENSION"
            ExportFileType.PDF -> "expense_manager_${timestamp}$PDF_FILE_EXTENSION"
        }
    }

    companion object {
        private const val CSV_FILE_EXTENSION = ".csv"
        private const val PDF_FILE_EXTENSION = ".pdf"
    }
}

private fun Double.toTrimAmount(): String {
    return String.format(Locale.getDefault(), "%.2f", this)
}
