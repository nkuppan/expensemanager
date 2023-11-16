package com.naveenapps.expensemanager.core.domain.usecase.settings.daterange

import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock


class GetDateRangeUseCaseTest {

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