package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class GetTransactionsGroupByDateUseCase @Inject constructor(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {
    fun invoke(): Flow<Map<String, List<Transaction>>?> {
        return getTransactionWithFilterUseCase.invoke().map {
            it?.groupBy { transaction ->
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                    transaction.createdOn
                )
            } ?: emptyMap()
        }
    }
}