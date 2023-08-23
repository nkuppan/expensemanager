package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.model.TransactionHistory
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
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