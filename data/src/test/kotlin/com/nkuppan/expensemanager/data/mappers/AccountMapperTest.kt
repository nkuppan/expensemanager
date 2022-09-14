package com.nkuppan.expensemanager.data.mappers

import com.google.common.truth.Truth
import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.PaymentMode
import org.junit.Before
import org.junit.Test
import java.util.*

class AccountMapperTest {

    private lateinit var accountDomainEntityMapper: AccountDomainEntityMapper

    @Before
    fun setup() {
        accountDomainEntityMapper = AccountDomainEntityMapper()
    }

    @Test
    fun checkDomainMapperObject() {
        Truth.assertThat(accountDomainEntityMapper).isNotNull()
    }

    @Test
    fun checkDomainMapperSuccessConversion() {

        val currentTimeStamp = Date()

        val domainModel = Account(
            "1",
            "Super",
            PaymentMode.UPI,
            "#FFFFFF",
            currentTimeStamp,
            currentTimeStamp,
        )
        val entityModel = accountDomainEntityMapper.convert(
            domainModel
        )

        Truth.assertThat(entityModel).isNotNull()
        Truth.assertThat(entityModel.id).isEqualTo(domainModel.id)
    }
}