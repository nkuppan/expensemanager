package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetFilterRangeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class GetFilterRangeUseCaseTest {

    private lateinit var getGetFilterRangeUseCase: GetFilterRangeUseCase

    @Before
    fun init() {
        getGetFilterRangeUseCase = GetFilterRangeUseCase(mock())
    }

    @Test
    fun whenUserRequestTodayDateTimeRange() = runTest {
        val result = getGetFilterRangeUseCase.invoke(DateRangeFilterType.TODAY)
        print(result)
    }
}