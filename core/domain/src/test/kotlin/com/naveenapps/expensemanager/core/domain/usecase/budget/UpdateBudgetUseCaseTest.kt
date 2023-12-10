package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class UpdateBudgetUseCaseTest : BaseCoroutineTest() {

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

    private val accountRepository: BudgetRepository = mock()
    private val checkBudgetValidationUseCase = CheckBudgetValidateUseCase()
    private lateinit var updateBudgetUseCase: UpdateBudgetUseCase

    override fun onCreate() {
        super.onCreate()

        updateBudgetUseCase = UpdateBudgetUseCase(
            accountRepository,
            checkBudgetValidationUseCase,
        )
    }

    @Test
    fun whenBudgetIsValidShouldUpdateSuccessfully() = runTest {
        whenever(accountRepository.updateBudget(budget)).thenReturn(Resource.Success(true))

        val response = updateBudgetUseCase.invoke(budget)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun whenBudgetIsInValidShouldReturnError() = runTest {
        val response = updateBudgetUseCase.invoke(budget.copy(id = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetNameIsInValidShouldReturnError() = runTest {
        val response = updateBudgetUseCase.invoke(budget.copy(name = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconNameIsInValidShouldReturnError() = runTest {
        val response =
            updateBudgetUseCase.invoke(budget.copy(storedIcon = StoredIcon("", "#ffffff")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconBGColorIsInValidShouldReturnError() = runTest {
        val response =
            updateBudgetUseCase.invoke(budget.copy(storedIcon = StoredIcon("ic_calendar", "")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetStoredIconBGColorIsInValidColorShouldReturnError() = runTest {
        val response = updateBudgetUseCase.invoke(
            budget.copy(storedIcon = StoredIcon("ic_calendar", "sample")),
        )
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetAccountsNotAvailableShouldReturnError() = runTest {
        val response = updateBudgetUseCase.invoke(
            budget.copy(
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
        val response = updateBudgetUseCase.invoke(
            budget.copy(
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
