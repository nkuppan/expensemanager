package com.naveenapps.expensemanager.core.domain.usecase.account

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class FindAccountByIdUseCaseTest : BaseCoroutineTest() {

    private val account = Account(
        id = "1",
        name = "Sample",
        type = AccountType.CREDIT,
        storedIcon = StoredIcon("ic_account", "#ffffff"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private val accountRepository: AccountRepository = mock()

    private lateinit var findAccountByIdUseCase: FindAccountByIdUseCase

    override fun onCreate() {
        super.onCreate()

        findAccountByIdUseCase = FindAccountByIdUseCase(accountRepository)
    }

    @Test
    fun whenAccountIsValidShouldReturnValidAccountSuccessfully() = runTest {
        whenever(accountRepository.findAccount(account.id)).thenReturn(Resource.Success(account))

        val response = findAccountByIdUseCase.invoke(account.id)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isEqualTo(account)
    }

    @Test
    fun whenAccountIdIsInEmptyShouldReturnError() = runTest {
        val response = findAccountByIdUseCase.invoke("")
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountIdIsInNullShouldReturnError() = runTest {
        val response = findAccountByIdUseCase.invoke(null)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
