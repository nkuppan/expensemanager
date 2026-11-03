package com.naveenapps.expensemanager.core.domain.usecase.account

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdateAccountUseCaseTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val checkAccountValidationUseCase = CheckAccountValidationUseCase()

    private lateinit var updateAccountUseCase: UpdateAccountUseCase

    override fun onCreate() {
        super.onCreate()

        updateAccountUseCase = UpdateAccountUseCase(
            accountRepository,
            checkAccountValidationUseCase,
        )
    }

    @Test
    fun whenAccountIsValidShouldUpdateSuccessfully() = runTest {
        whenever(accountRepository.findAccount(FAKE_ACCOUNT.id)).thenReturn(Resource.Success(FAKE_ACCOUNT))
        whenever(accountRepository.updateAccount(FAKE_ACCOUNT)).thenReturn(Resource.Success(true))

        val response = updateAccountUseCase.invoke(FAKE_ACCOUNT)
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((response as Resource.Success).data).isTrue()
    }

    @Test
    fun whenAccountIsInValidShouldReturnError() = runTest {
        val response = updateAccountUseCase.invoke(FAKE_ACCOUNT.copy(id = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountNameIsInValidShouldReturnError() = runTest {
        val response = updateAccountUseCase.invoke(FAKE_ACCOUNT.copy(name = ""))
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountStoredIconNameIsInValidShouldReturnError() = runTest {
        val response = updateAccountUseCase.invoke(
            FAKE_ACCOUNT.copy(storedIcon = StoredIcon("", "#ffffff")),
        )
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenAccountStoredIconBGColorIsInValidShouldReturnError() = runTest {
        val response = updateAccountUseCase.invoke(
            FAKE_ACCOUNT.copy(
                storedIcon = StoredIcon(
                    "ic_calendar",
                    "",
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
    fun whenAccountStoredIconBGColorIsInValidColorShouldReturnError() = runTest {
        val response = updateAccountUseCase.invoke(
            FAKE_ACCOUNT.copy(
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
    fun whenOnlyAppearanceChangesShouldUpdate() = runTest {
        val recolored = FAKE_ACCOUNT.copy(storedIcon = FAKE_ACCOUNT.storedIcon.copy(backgroundColor = "#000000"))
        whenever(accountRepository.findAccount(FAKE_ACCOUNT.id)).thenReturn(Resource.Success(FAKE_ACCOUNT))
        whenever(accountRepository.updateAccount(recolored)).thenReturn(Resource.Success(true))

        val response = updateAccountUseCase.invoke(recolored)

        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun whenLockedFieldChangesShouldRefuse() = runTest {
        whenever(accountRepository.findAccount(FAKE_ACCOUNT.id)).thenReturn(Resource.Success(FAKE_ACCOUNT))

        listOf(
            FAKE_ACCOUNT.copy(name = "Renamed"),
            FAKE_ACCOUNT.copy(currencyCode = "EUR"),
            FAKE_ACCOUNT.copy(amount = 999.0),
            FAKE_ACCOUNT.copy(type = AccountType.CREDIT),
            FAKE_ACCOUNT.copy(creditLimit = 10.0),
        ).forEach { changed ->
            val response = updateAccountUseCase.invoke(changed)
            Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        }
        verify(accountRepository, never()).updateAccount(any())
    }

    @Test
    fun whenAccountIsArchivedShouldRefuse() = runTest {
        whenever(accountRepository.findAccount(FAKE_ACCOUNT.id))
            .thenReturn(Resource.Success(FAKE_ACCOUNT.copy(isArchived = true)))

        val response = updateAccountUseCase.invoke(FAKE_ACCOUNT)

        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
    }
}
