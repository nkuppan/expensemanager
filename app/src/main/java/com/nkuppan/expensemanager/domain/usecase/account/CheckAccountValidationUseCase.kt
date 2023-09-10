package com.nkuppan.expensemanager.domain.usecase.account

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.PaymentMode
import com.nkuppan.expensemanager.domain.model.Resource
import javax.inject.Inject

class CheckAccountValidationUseCase @Inject constructor() {

    operator fun invoke(account: Account): Resource<Boolean> {

        if (account.id.isBlank()) {
            return Resource.Error(Exception("ID shouldn't be blank"))
        }

        if (account.backgroundColor.isBlank()) {
            return Resource.Error(Exception("Background color shouldn't be blank"))
        }

        if (!account.backgroundColor.startsWith("#")) {
            return Resource.Error(Exception("Background color is not valid"))
        }

        if (account.name.isBlank()) {
            return Resource.Error(Exception("Account name shouldn't be blank"))
        }

        if (account.type == PaymentMode.NONE) {
            return Resource.Error(Exception("Account type shouldn't be none"))
        }

        return Resource.Success(true)
    }
}