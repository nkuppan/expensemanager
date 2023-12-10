package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import org.junit.Test
import java.util.Date

class CheckBudgetValidateUseCaseTest : BaseCoroutineTest() {

    private val budget = Budget(
        id = "1",
        name = "Sample",
        amount = 5000.0,
        selectedMonth = "11/2023",
        accounts = emptyList(),
        categories = emptyList(),
        isAllAccountsSelected = true,
        isAllCategoriesSelected = true,
        storedIcon = StoredIcon("ic_account", "#ffffff"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private var checkBudgetValidateUseCase = CheckBudgetValidateUseCase()

    @Test
    fun whenProperBudgetShouldReturnSuccess() {
        val response = checkBudgetValidateUseCase.invoke(budget)

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        val success = (response as Resource.Success)
        Truth.assertThat(success).isNotNull()
        Truth.assertThat(success.data).isTrue()
    }

    @Test
    fun whenBudgetIdIsNotAvailableShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(id = ""),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenStoredIconNameNotAvailableShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(
                storedIcon = StoredIcon(
                    name = "",
                    backgroundColor = "#ffff",
                ),
            ),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenStoredIconBGColorNotAvailableShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(
                storedIcon = StoredIcon(
                    name = "Sample",
                    backgroundColor = "",
                ),
            ),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenStoredIconBGColorNotStartsWithHashShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(
                storedIcon = StoredIcon(
                    name = "Sample",
                    backgroundColor = "sample",
                ),
            ),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetNameIsNotAvailableShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(name = ""),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetsEmptyForShouldReturnError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(isAllAccountsSelected = false, accounts = emptyList()),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenCategoriesEmptyForShouldReturnError() {
        val response = checkBudgetValidateUseCase.invoke(
            budget.copy(isAllCategoriesSelected = false, categories = emptyList()),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
