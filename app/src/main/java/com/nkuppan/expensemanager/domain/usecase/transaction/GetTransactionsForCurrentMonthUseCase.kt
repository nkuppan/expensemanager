package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.data.utils.getDateTime
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetTransactionsForCurrentMonthUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun invoke(): Flow<Map<String, List<Transaction>>?> {

        val filterTypeRanges = getThisMonthValues()

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


private fun getThisMonthValues(): List<Long> {

    val startDate = getDateTime()
    startDate.set(Calendar.DAY_OF_MONTH, 1)
    val fromDate = startDate.timeInMillis

    startDate.set(Calendar.DAY_OF_MONTH, startDate.getActualMaximum(Calendar.DAY_OF_MONTH))
    startDate.set(Calendar.HOUR_OF_DAY, 24)
    val toDate = startDate.timeInMillis

    return listOf(fromDate, toDate)
}