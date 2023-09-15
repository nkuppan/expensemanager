package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.data.utils.getPreviousDateTime
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetPreviousDaysTransactionWithFilterUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(numberOfDays: Int): Flow<Map<String, List<Transaction>>> {

        return settingsRepository.getAccountId().flatMapLatest { accountId ->

            val isValidAccount = accountId?.isNotBlank() == true && accountId != "-1"

            if (isValidAccount) {
                transactionRepository.getTransactionsByAccountId(accountId!!)
            } else {
                transactionRepository.getAllTransaction()
            }
        }.map { response ->

            val lastSevenDays = getPreviousDateTime(numberOfDays)

            return@map response?.filter {
                it.createdOn.time > lastSevenDays.timeInMillis
            }?.groupBy { transactionCategory ->
                SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(
                    transactionCategory.createdOn
                )
            } ?: emptyMap()
        }
    }
}