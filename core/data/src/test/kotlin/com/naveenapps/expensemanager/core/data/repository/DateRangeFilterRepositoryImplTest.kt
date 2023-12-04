package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.DateRangeDataStore
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DateRangeFilterRepositoryImplTest : BaseCoroutineTest() {

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher.dispatcher)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = {
                testContext.preferencesDataStoreFile("TEST_DATASTORE_NAME")
            },
        )

    private val repository: DateRangeFilterRepository = DateRangeFilterRepositoryImpl(
        DateRangeDataStore(testDataStore),
        AppCoroutineDispatchers(
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
        ),
    )

    @Test
    fun getDateRangeValueShouldReturnDefault() = runTest {
        repository.getDateRangeFilterType().test {
            val type = awaitItem()
            Truth.assertThat(type).isNotNull()
            Truth.assertThat(type).isEqualTo(DateRangeType.THIS_MONTH)
        }
    }

    @Test
    fun getDateRangeValueShouldUpdateOnEachUpdate() = runTest {
        repository.getDateRangeFilterType().test {
            val type = awaitItem()
            Truth.assertThat(type).isNotNull()
            Truth.assertThat(type).isEqualTo(DateRangeType.THIS_MONTH)

            repository.setDateRangeFilterType(DateRangeType.ALL)

            val updatedType = awaitItem()
            Truth.assertThat(updatedType).isNotNull()
            Truth.assertThat(updatedType).isEqualTo(DateRangeType.ALL)
        }
    }

    @Test
    fun getAllDateRangesShouldAllTypesAndNames() = runTest {
        val response = repository.getAllDateRanges()
        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        val data = (response as Resource.Success).data
        Truth.assertThat(data).isNotEmpty()
        Truth.assertThat(data).hasSize(DateRangeType.entries.size)
        val firstItem = data.first()
        Truth.assertThat(firstItem).isNotNull()
        Truth.assertThat(firstItem.type).isEqualTo(DateRangeType.entries[0])
    }

    @Test
    fun getDateRangeTimeFrameShouldReturnTimeFrame() = runTest {
        repository.getDateRangeTimeFrame().test {
            val dateRanges = awaitItem()
            Truth.assertThat(dateRanges).isNull()

            val startDate = Date()
            val endDate = Date()
            val dateRangesValues = listOf(startDate, endDate)
            repository.setDateRanges(dateRangesValues)

            val updatedRanges = awaitItem()
            Truth.assertThat(updatedRanges).isNotEmpty()
            Truth.assertThat(updatedRanges).hasSize(2)
            Truth.assertThat(updatedRanges?.get(0)).isEqualTo(startDate.time)
            Truth.assertThat(updatedRanges?.get(1)).isEqualTo(endDate.time)
        }
    }

    @Test
    fun getDateRangeGroupShouldReturnTheType() = runTest {
        val todayType = repository.getTransactionGroupType(DateRangeType.TODAY)
        Truth.assertThat(todayType).isEqualTo(GroupType.DATE)

        val thisWeekType = repository.getTransactionGroupType(DateRangeType.THIS_WEEK)
        Truth.assertThat(thisWeekType).isEqualTo(GroupType.DATE)

        val thisMonthType = repository.getTransactionGroupType(DateRangeType.THIS_MONTH)
        Truth.assertThat(thisMonthType).isEqualTo(GroupType.DATE)

        val thisYearType = repository.getTransactionGroupType(DateRangeType.THIS_YEAR)
        Truth.assertThat(thisYearType).isEqualTo(GroupType.MONTH)

        val customType = repository.getTransactionGroupType(DateRangeType.CUSTOM)
        Truth.assertThat(customType).isEqualTo(GroupType.DATE)

        val allType = repository.getTransactionGroupType(DateRangeType.CUSTOM)
        Truth.assertThat(allType).isEqualTo(GroupType.DATE)
    }
}
