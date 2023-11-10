package com.naveenapps.expensemanager.domain.usecase.account

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class CheckAccountValidationUseCase @Inject constructor() {

    operator fun invoke(account: Account): Resource<Boolean> {

        if (account.id.isBlank()) {
            return Resource.Error(Exception("ID shouldn't be blank"))
        }

        if (account.iconBackgroundColor.isBlank()) {
            return Resource.Error(Exception("Background color shouldn't be blank"))
        }

        if (!account.iconBackgroundColor.startsWith("#")) {
            return Resource.Error(Exception("Background color is not valid"))
        }

        if (account.name.isBlank()) {
            return Resource.Error(Exception("Account name shouldn't be blank"))
        }

        return Resource.Success(true)
    }
}