package com.nkuppan.expensemanager.domain.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import javax.inject.Inject

class GetAccountsByNameUseCase @Inject constructor(
    private val getAllAccountsUseCase: GetAllAccountsUseCase
) {

    suspend operator fun invoke(accountName: String?): Resource<List<Account>> {
        return when (val response = getAllAccountsUseCase.invoke()) {
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