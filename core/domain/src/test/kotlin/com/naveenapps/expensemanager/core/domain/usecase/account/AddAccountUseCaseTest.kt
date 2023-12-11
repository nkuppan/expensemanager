package com.naveenapps.expensemanager.core.domain.usecase.account

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddAccountUseCaseTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val checkAccountValidationUseCase = CheckAccountValidationUseCase()

    private lateinit var addAccountUseCase: AddAccountUseCase

    override fun onCreate() {
        super.onCreate()

        addAccountUseCase = AddAccountUseCase(
            accountRepository,
            checkAccountValidationUseCase,
        )
    }

    @Test
    fun whenAccountIsValidShouldAddSuccessfully() = runTest {
        whenever(accountRepository.addAccount(FAKE_ACCOUNT)).thenReturn(Resource.Success(true))

        val response = addAccountUseCase.invoke(FAKE_ACCOUNT)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun whenAccountIsInValidShouldReturnError() = runTest {
        val response = addAccountUseCase.invoke(FAKE_ACCOUNT.copy(id = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountNameIsInValidShouldReturnError() = runTest {
        val response = addAccountUseCase.invoke(FAKE_ACCOUNT.copy(name = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountStoredIconNameIsInValidShouldReturnError() = runTest {
        val response =
            addAccountUseCase.invoke(FAKE_ACCOUNT.copy(storedIcon = StoredIcon("", "#ffffff")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountStoredIconBGColorIsInValidShouldReturnError() = runTest {
        val response =
            addAccountUseCase.invoke(FAKE_ACCOUNT.copy(storedIcon = StoredIcon("ic_calendar", "")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountStoredIconBGColorIsInValidColorShouldReturnError() = runTest {
        val response =
            addAccountUseCase.invoke(FAKE_ACCOUNT.copy(storedIcon = StoredIcon("ic_calendar", "sample")))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
