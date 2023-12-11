package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import org.junit.Test

class CheckBudgetValidateUseCaseTest : BaseCoroutineTest() {

    private var checkBudgetValidateUseCase = CheckBudgetValidateUseCase()

    @Test
    fun whenProperBudgetShouldReturnSuccess() {
        val response = checkBudgetValidateUseCase.invoke(FAKE_BUDGET)

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        val success = (response as Resource.Success)
        Truth.assertThat(success).isNotNull()
        Truth.assertThat(success.data).isTrue()
    }

    @Test
    fun whenBudgetIdIsNotAvailableShouldResultError() {
        val response = checkBudgetValidateUseCase.invoke(
            FAKE_BUDGET.copy(id = ""),
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
            FAKE_BUDGET.copy(
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
            FAKE_BUDGET.copy(
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
            FAKE_BUDGET.copy(
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
            FAKE_BUDGET.copy(name = ""),
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
            FAKE_BUDGET.copy(isAllAccountsSelected = false, accounts = emptyList()),
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
            FAKE_BUDGET.copy(isAllCategoriesSelected = false, categories = emptyList()),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
