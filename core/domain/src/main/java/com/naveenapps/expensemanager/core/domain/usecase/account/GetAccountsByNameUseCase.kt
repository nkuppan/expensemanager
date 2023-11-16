package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
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