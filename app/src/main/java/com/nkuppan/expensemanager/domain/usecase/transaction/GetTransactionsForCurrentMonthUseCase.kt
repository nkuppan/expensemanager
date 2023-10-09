package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.data.utils.getThisMonthRange
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class GetTransactionsForCurrentMonthUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun invoke(): Flow<Map<String, List<Transaction>>?> {

        val filterTypeRanges = getThisMonthRange()

        return transactionRepository.getTransactionByDateFilter(
            filterTypeRanges[0],
            filterTypeRanges[1]
        ).map {
            it.groupBy { transaction ->
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                    transaction.createdOn
                )
            }
        }
    }
}