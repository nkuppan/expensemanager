package com.nkuppan.expensemanager.data.usecase.account

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.AccountRepository
import javax.inject.Inject

class GetAccountsByNameUseCase @Inject constructor(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountName: String?): Resource<List<Account>> {
        return when (val response = repository.getAllAccount()) {
            is Resource.Error -> response
            is Resource.Success -> {

                val values = response.data

                val filteredList = mutableListOf<Account>()

                if (accountName?.isNotBlank() == true) {
                    filteredList.addAll(values.filter {
                        it.name.contains(accountName, ignoreCase = true)
                    })
                } else {
                    filteredList.addAll(values)
                }

                Resource.Success(filteredList)
            }
        }
    }
}