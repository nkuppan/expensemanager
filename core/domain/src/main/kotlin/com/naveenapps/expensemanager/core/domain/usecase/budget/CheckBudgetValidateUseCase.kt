package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class CheckBudgetValidateUseCase @Inject constructor() {

    operator fun invoke(budget: Budget): Resource<Boolean> {
        if (budget.id.isBlank()) {
            return Resource.Error(Exception("Please specify the budget id"))
        }

        if (budget.name.isBlank()) {
            return Resource.Error(Exception("Budget name shouldn't be empty"))
        }

        if (budget.isAllAccountsSelected.not() && budget.accounts.isEmpty()) {
            return Resource.Error(Exception("Accounts shouldn't be empty"))
        }

        if (budget.isAllCategoriesSelected.not() && budget.categories.isEmpty()) {
            return Resource.Error(Exception("Categories shouldn't be empty"))
        }

        if (budget.storedIcon.name.isBlank()) {
            return Resource.Error(Exception("Background name is not available"))
        }

        if (budget.storedIcon.backgroundColor.isBlank()) {
            return Resource.Error(Exception("Background color is not available"))
        }

        if (!budget.storedIcon.backgroundColor.startsWith("#")) {
            return Resource.Error(Exception("Background color is not valid"))
        }

        if (budget.amount <= 0.0) {
            return Resource.Error(Exception("Amount shouldn't be zero"))
        }

        return Resource.Success(true)
    }
}
