package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.repository.DateRangeFilterRepository
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetDateRangeByTypeUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetDateRangeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class GetFilterRangeUseCaseTest {

    private lateinit var getGetFilterRangeUseCase: GetDateRangeUseCase

    @Before
    fun init() {
        val dateRangeFilterRepository = mock<DateRangeFilterRepository>()
        getGetFilterRangeUseCase = GetDateRangeUseCase(
            GetDateRangeByTypeUseCase(dateRangeFilterRepository),
            dateRangeFilterRepository
        )
    }

    @Test
    fun whenUserRequestTodayDateTimeRange() = runTest {
        val result = getGetFilterRangeUseCase.invoke()
        print(result)
    }
}