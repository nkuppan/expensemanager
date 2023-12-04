package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTransactionByNameUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    fun invoke(searchText: String?): Flow<List<Transaction>> {
        return repository.getAllTransaction().map { values ->
            val filteredList = mutableListOf<Transaction>()

            if (searchText?.isNotBlank() == true && values?.isNotEmpty() == true) {
                filteredList.addAll(
                    values.filter {
                        val noteContainSearchText = it.notes.contains(
                            searchText,
                            ignoreCase = true,
                        )

                        val categoryNameContainSearchText = it.category.name.contains(
                            searchText,
                            ignoreCase = true,
                        )

                        noteContainSearchText || categoryNameContainSearchText
                    },
                )
            } else {
                filteredList.addAll(values?.toList() ?: emptyList())
            }

            filteredList
        }
    }
}
