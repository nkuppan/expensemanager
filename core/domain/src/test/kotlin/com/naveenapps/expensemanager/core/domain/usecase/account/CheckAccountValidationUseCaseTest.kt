package com.naveenapps.expensemanager.core.domain.usecase.account

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import org.junit.Test
import java.util.Date

class CheckAccountValidationUseCaseTest : BaseCoroutineTest() {

    private val account = Account(
        id = "1",
        name = "Sample",
        type = AccountType.CREDIT,
        storedIcon = StoredIcon("ic_account", "#ffffff"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private var checkAccountValidationUseCase = CheckAccountValidationUseCase()

    @Test
    fun whenProperAccountShouldReturnSuccess() {
        val response = checkAccountValidationUseCase.invoke(account)

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        val success = (response as Resource.Success)
        Truth.assertThat(success).isNotNull()
        Truth.assertThat(success.data).isTrue()
    }

    @Test
    fun whenAccountIdIsNotAvailableShouldResultError() {
        val response = checkAccountValidationUseCase.invoke(
            account.copy(id = ""),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }

    @Test
    fun whenStoredIconNameNotAvailableShouldResultError() {
        val response = checkAccountValidationUseCase.invoke(
            account.copy(
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
        val response = checkAccountValidationUseCase.invoke(
            account.copy(
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
        val response = checkAccountValidationUseCase.invoke(
            account.copy(
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
    fun whenAccountNameIsNotAvailableShouldResultError() {
        val response = checkAccountValidationUseCase.invoke(
            account.copy(name = ""),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Error::class.java)
        val exception = (response as Resource.Error).exception
        Truth.assertThat(exception).isNotNull()
        Truth.assertThat(exception.message).isNotEmpty()
    }
}
