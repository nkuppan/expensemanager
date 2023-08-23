package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.repository.SettingsRepository
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetTransactionWithFilterUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(): Flow<List<Transaction>?> {
        return settingsRepository.getAccountId()
            .combine(settingsRepository.getFilterType()) { accountId, filterType ->

                val filterTypeRanges = settingsRepository.getFilterRange(filterType)

                val isValidAccount = accountId?.isNotBlank() == true && accountId != "-1"

                val flows = if (filterType == FilterType.ALL) {
                    if (isValidAccount) {
                        transactionRepository.getTransactionsByAccountId(accountId!!)
                    } else {
                        transactionRepository.getAllTransaction()
                    }
                } else {
                    if (isValidAccount) {
                        transactionRepository.getTransactionByAccountIdAndDateFilter(
                            accountId!!,
                            filterTypeRanges[0],
                            filterTypeRanges[1]
                        )
                    } else {
                        transactionRepository.getTransactionByDateFilter(
                            filterTypeRanges[0],
                            filterTypeRanges[1]
                        )
                    }
                }
                return@combine flows
            }.flatMapLatest {
                it
            }
    }
}