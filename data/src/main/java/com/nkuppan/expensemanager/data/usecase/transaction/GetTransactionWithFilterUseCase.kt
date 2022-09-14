package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.FilterType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.repository.SettingsRepository
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first

class GetTransactionWithFilterUseCase(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend fun invoke(): Resource<List<Transaction>> {

        val accountId = settingsRepository.getAccountId().first()

        val filterType = settingsRepository.getFilterType().first()

        val isValidAccount = accountId.isNotBlank()

        return if (filterType == FilterType.ALL) {
            if (isValidAccount) {
                transactionRepository.getTransactionsByAccountId(accountId)
            } else {
                transactionRepository.getAllTransaction()
            }
        } else {

            val dateRange = settingsRepository.getFilterRange(filterType)

            if (isValidAccount) {
                transactionRepository.getTransactionByAccountIdAndDateFilter(
                    accountId,
                    dateRange[0],
                    dateRange[1]
                )
            } else {
                transactionRepository.getTransactionByDateFilter(
                    dateRange[0],
                    dateRange[1]
                )
            }
        }
    }
}