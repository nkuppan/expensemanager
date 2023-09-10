package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.TransactionHistory
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetTransactionGroupByMonthUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun invoke(): Flow<List<TransactionHistory>> {

        return transactionRepository.getAllTransaction().map { data ->

            val groupedValue: Map<String, List<Transaction>> =
                data?.groupBy { transactionCategory ->
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(
                        transactionCategory.createdOn
                    )
                } ?: emptyMap()

            groupedValue.map {
                TransactionHistory(
                    it.value,
                    it.key.toDateValue(),
                    it.key
                )
            }.sortedBy { it.monthTime }.toList()
        }
    }
}

fun String.toDateValue(): Long {
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(this)?.time ?: Date().time
}