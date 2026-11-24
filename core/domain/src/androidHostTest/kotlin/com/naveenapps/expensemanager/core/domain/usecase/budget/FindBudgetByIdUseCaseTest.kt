package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FindBudgetByIdUseCaseTest : BaseCoroutineTest() {

    private val budgetRepository: BudgetRepository = mock()

    private lateinit var findBudgetByIdUseCase: FindBudgetByIdUseCase

    override fun onCreate() {
        super.onCreate()

        findBudgetByIdUseCase = FindBudgetByIdUseCase(budgetRepository)
    }

    @Test
    fun whenBudgetIsValidShouldReturnValidBudgetSuccessfully() = runTest {
        whenever(budgetRepository.findBudgetById(FAKE_BUDGET.id)).thenReturn(
            Resource.Success(
                FAKE_BUDGET,
            ),
        )

        val response = findBudgetByIdUseCase.invoke(FAKE_BUDGET.id)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isEqualTo(FAKE_BUDGET)
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
