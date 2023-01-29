package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionByNameUseCase(private val repository: TransactionRepository) {
    fun invoke(searchText: String?): Flow<List<Transaction>> {
        return repository.getAllTransaction().map { values ->
            val filteredList = mutableListOf<Transaction>()

            if (searchText?.isNotBlank() == true && values?.isNotEmpty() == true) {

                filteredList.addAll(values.filter {

                    val noteContainSearchText = it.notes.contains(
                        searchText,
                        ignoreCase = true
                    )

                    val categoryNameContainSearchText = it.category.name.contains(
                        searchText, ignoreCase = true
                    )

                    noteContainSearchText || categoryNameContainSearchText
                })
            } else {
                filteredList.addAll(values?.toList() ?: emptyList())
            }

            filteredList
        }
    }
}