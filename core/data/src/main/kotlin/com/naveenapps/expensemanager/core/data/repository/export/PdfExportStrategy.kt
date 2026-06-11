package com.naveenapps.expensemanager.core.data.repository.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

class PdfExportStrategy(context: Context) : BaseExportStrategy(context) {

    override fun getFileType(): ExportFileType = ExportFileType.PDF

    override fun export(uri: String?, transactions: List<Transaction>): Resource<ExportData> {
        return kotlin.runCatching {
            val fileUri = generateFile(getFileName(), uri) { output ->
                buildPdf(transactions).also { doc ->
                    doc.writeTo(output)
                    doc.close()
                }
            }
            Resource.Success(fileUri)
        }.onFailure {
            Log.e("PdfExportStrategy", it.localizedMessage ?: "")
        }.getOrNull() ?: Resource.Error(Exception())
    }

    private fun buildPdf(transactions: List<Transaction>): PdfDocument {
        // A4 landscape: 842 x 595 pts
        val pageWidth = 842
        val pageHeight = 595
        val margin = 32f
        val contentWidth = pageWidth - margin * 2  // 778

        val fixedWidths = floatArrayOf(85f, 38f, 55f, 75f, 55f, 75f, 55f, 65f, 55f)
        val colWidths = fixedWidths + floatArrayOf(contentWidth - fixedWidths.sum())

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
                c.drawText(
                    clipText(label, colWidths[i] - 6f, headerTextPaint),
                    x,
                    yTop + 15f,
                    headerTextPaint
                )
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
                c.drawText(
                    clipText(cell, colWidths[i] - 6f, paint),
                    x,
                    yTop + rowHeight - 5f,
                    paint
                )
                x += colWidths[i]
            }
            c.drawLine(
                margin,
                yTop + rowHeight,
                margin + contentWidth,
                yTop + rowHeight,
                dividerPaint
            )
        }

        val totalIncome =
            transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.amount }
        val totalExpense =
            transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.amount }
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

        canvas.drawText("Expense Manager", margin, y + 14f, titlePaint)
        y += 26f

        fun summaryRow(label: String, value: String) {
            canvas.drawText(label, margin, y, summaryLabelPaint)
            canvas.drawText(value, margin + 170f, y, summaryValuePaint)
            y += 14f
        }
        summaryRow(
            context.getString(R.string.exported_on),
            "${now.toCompleteDate()} ${now.toTimeAndMinutes()}"
        )
        summaryRow(context.getString(R.string.total_transactions), transactions.size.toString())
        summaryRow(context.getString(R.string.total_income), totalIncome.toTrimAmount())
        summaryRow(context.getString(R.string.total_expense), totalExpense.toTrimAmount())
        summaryRow(
            context.getString(R.string.net_balance),
            (totalIncome - totalExpense).toTrimAmount()
        )

        y += 4f
        canvas.drawLine(margin, y, margin + contentWidth, y, dividerPaint)
        y += 10f

        drawColumnHeaders(canvas, y)
        y += headerRowHeight

        var pageRowIndex = 0
        transactions.forEach { transaction ->
            if (y + rowHeight > pageHeight - margin) {
                document.finishPage(page)
                val next = newPage()
                page = next.first
                canvas = next.second
                y = margin
                drawColumnHeaders(canvas, y)
                y += headerRowHeight
                pageRowIndex = 0
            }
            drawDataRow(canvas, transaction, pageRowIndex++, y)
            y += rowHeight
        }

        document.finishPage(page)
        return document
    }
}
