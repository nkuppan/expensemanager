package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
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