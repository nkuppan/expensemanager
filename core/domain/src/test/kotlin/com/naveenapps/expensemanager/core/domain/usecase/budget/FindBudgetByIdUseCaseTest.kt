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

class FindBudgetByIdUseCaseTest : BaseCoroutineTest() {

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

    private val budgetRepository: BudgetRepository = mock()

    private lateinit var findBudgetByIdUseCase: FindBudgetByIdUseCase

    override fun onCreate() {
        super.onCreate()

        findBudgetByIdUseCase = FindBudgetByIdUseCase(budgetRepository)
    }

    @Test
    fun whenBudgetIsValidShouldReturnValidBudgetSuccessfully() = runTest {
        whenever(budgetRepository.findBudgetById(budget.id)).thenReturn(Resource.Success(budget))

        val response = findBudgetByIdUseCase.invoke(budget.id)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isEqualTo(budget)
    }

    @Test
    fun whenBudgetIdIsInEmptyShouldReturnError() = runTest {
        val response = findBudgetByIdUseCase.invoke("")
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenBudgetIdIsInNullShouldReturnError() = runTest {
        val response = findBudgetByIdUseCase.invoke(null)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
