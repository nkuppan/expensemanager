package com.nkuppan.expensemanager.data.usecase.transaction

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.repository.TransactionRepository

class GetTransactionByNameUseCase(private val repository: TransactionRepository) {
    suspend fun invoke(searchText: String?): Resource<List<Transaction>> {
        return when (val response = repository.getAllTransaction()) {
            is Resource.Error -> response
            is Resource.Success -> {

                val values = response.data

                val filteredList = mutableListOf<Transaction>()

                if (searchText?.isNotBlank() == true && values.isNotEmpty()) {
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
                    filteredList.addAll(values)
                }

                Resource.Success(filteredList)
            }
        }
    }
}