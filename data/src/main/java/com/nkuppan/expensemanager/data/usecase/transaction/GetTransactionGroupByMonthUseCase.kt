package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.model.TransactionHistory
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.*

class GetTransactionGroupByMonthUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend fun invoke(): Resource<List<TransactionHistory>> {

        return when (val response = transactionRepository.getAllTransaction()) {
            is Resource.Error -> {
                Resource.Error(response.exception)
            }
            is Resource.Success -> {
                val data = response.data

                val groupedValue: Map<String, List<Transaction>> =
                    data.groupBy { transactionCategory ->
                        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(
                            transactionCategory.createdOn
                        )
                    }

                return Resource.Success(groupedValue.map {
                    TransactionHistory(
                        it.value,
                        it.key.toDateValue(),
                        it.key
                    )
                }.sortedBy { it.monthTime }.toList())
            }

        }
    }
}

fun String.toDateValue(): Long {
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(this)?.time ?: Date().time
}