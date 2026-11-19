package com.naveenapps.expensemanager.core.data.repository.export

import android.content.Context
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
import com.opencsv.CSVWriter
import java.util.Date

class CsvExportStrategy(context: Context) : BaseExportStrategy(context) {

    override fun getFileType(): ExportFileType = ExportFileType.CSV

    override fun export(uri: String?, transactions: List<Transaction>): Resource<ExportData> = kotlin.runCatching {
        val fileUri = generateFile(getFileName(), uri) { output ->
            val writer = CSVWriter(output.writer())
            val data = mutableListOf<Array<String>>()

            val now = Date()

            data.add(
                arrayOf(
                    context.getString(R.string.exported_on),
                    "${now.toCompleteDate()} ${now.toTimeAndMinutes()}",
                ),
            )
            data.add(
                arrayOf(
                    context.getString(R.string.total_transactions),
                    transactions.size.toString(),
                ),
            )
            transactions.totalsByCurrency().forEach { totals ->
                val code = totals.currencyCode
                data.add(
                    arrayOf(
                        "${context.getString(R.string.total_income)} ($code)",
                        totals.income.toTrimAmount(),
                    ),
                )
                data.add(
                    arrayOf(
                        "${context.getString(R.string.total_expense)} ($code)",
                        totals.expense.toTrimAmount(),
                    ),
                )
                data.add(
                    arrayOf(
                        "${context.getString(R.string.net_balance)} ($code)",
                        totals.net.toTrimAmount(),
                    ),
                )
            }
            data.add(arrayOf())

            data.add(
                arrayOf(
                    context.getString(R.string.date),
                    context.getString(R.string.time),
                    context.getString(R.string.transaction_type),
                    context.getString(R.string.category),
                    context.getString(R.string.category_type),
                    context.getString(R.string.from_account),
                    context.getString(R.string.account_type),
                    context.getString(R.string.to_account),
                    context.getString(R.string.amount),
                    context.getString(R.string.currency),
                    context.getString(R.string.received_amount),
                    context.getString(R.string.received_currency),
                    context.getString(R.string.exchange_rate),
                    context.getString(R.string.notes),
                ),
            )

            transactions.forEach { transaction ->
                val signedAmount = when (transaction.type) {
                    TransactionType.EXPENSE -> -transaction.amount.amount
                    else -> transaction.amount.amount
                }
                data.add(
                    arrayOf(
                        transaction.createdOn.toCompleteDate(),
                        transaction.createdOn.toTimeAndMinutes(),
                        transaction.type.toCapitalize(),
                        (
                            transaction.category.titleResId?.let { context.getString(it) }
                                ?: transaction.category.name
                            ).toCsvSafeText(),
                        transaction.category.type.toCapitalize(),
                        transaction.fromAccount.name.toCsvSafeText(),
                        transaction.fromAccount.type.toCapitalize(),
                        (transaction.toAccount?.name ?: "").toCsvSafeText(),
                        signedAmount.toTrimAmount(),
                        transaction.fromCurrencyCode,
                        transaction.receivedAmount()?.toTrimAmount().orEmpty(),
                        if (transaction.rateSnapshot != null) transaction.toCurrencyCode else "",
                        transaction.rateSnapshot?.rate?.toString().orEmpty(),
                        transaction.notes.toCsvSafeText(),
                    ),
                )
            }

            writer.writeAll(data)
            writer.close()
        }
        Resource.Success(fileUri)
    }.onFailure {
        Log.e("CsvExportStrategy", it.localizedMessage ?: "")
    }.getOrNull() ?: Resource.Error(Exception())
}
