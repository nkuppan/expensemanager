package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddBudgetUseCaseTest : BaseCoroutineTest() {

    private val budgetRepository: BudgetRepository = mock()
    private val checkBudgetValidateUseCase = CheckBudgetValidateUseCase()
    private lateinit var addBudgetUseCase: AddBudgetUseCase

    override fun onCreate() {
        super.onCreate()

        addBudgetUseCase = AddBudgetUseCase(
            budgetRepository,
            checkBudgetValidateUseCase,
        )
    }

    @Test
    fun whenBudgetIsValidShouldAddSuccessfully() = runTest {
        whenever(budgetRepository.addBudget(FAKE_BUDGET)).thenReturn(Resource.Success(true))

        val response = addBudgetUseCase.invoke(FAKE_BUDGET)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun whenBudgetIsInValidShouldReturnError() = runTest {
        val response = addBudgetUseCase.invoke(FAKE_BUDGET.copy(id = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetNameIsInValidShouldReturnError() = runTest {
        val response = addBudgetUseCase.invoke(FAKE_BUDGET.copy(name = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconNameIsInValidShouldReturnError() = runTest {
        val response =
            addBudgetUseCase.invoke(FAKE_BUDGET.copy(storedIcon = StoredIcon("", "#ffffff")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconBGColorIsInValidShouldReturnError() = runTest {
        val response =
            addBudgetUseCase.invoke(FAKE_BUDGET.copy(storedIcon = StoredIcon("ic_calendar", "")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconBGColorIsInValidColorShouldReturnError() = runTest {
        val response =
            addBudgetUseCase.invoke(
                FAKE_BUDGET.copy(
                    storedIcon = StoredIcon(
                        "ic_calendar",
                        "sample",
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
    fun whenBudgetAccountsNotAvailableShouldReturnError() = runTest {
        val response = addBudgetUseCase.invoke(
            FAKE_BUDGET.copy(
                isAllAccountsSelected = false,
                accounts = emptyList(),
            ),
        )
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetCategoriesNotAvailableShouldReturnError() = runTest {
        val response = addBudgetUseCase.invoke(
            FAKE_BUDGET.copy(
                isAllCategoriesSelected = false,
                categories = emptyList(),
            ),
        )
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
