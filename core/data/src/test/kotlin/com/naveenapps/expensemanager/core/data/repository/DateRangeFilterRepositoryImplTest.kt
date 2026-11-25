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
import kotlin.time.Clock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import org.junit.runner.RunWith

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
        testContext,
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

            val startDate = Clock.System.now()
            val endDate = Clock.System.now()
            val dateRangesValues = listOf(startDate, endDate)
            repository.setDateRanges(dateRangesValues)

            val updatedRanges = awaitItem()
            Truth.assertThat(updatedRanges).isNotEmpty()
            Truth.assertThat(updatedRanges).hasSize(2)
            Truth.assertThat(updatedRanges?.get(0)).isEqualTo(startDate.toEpochMilliseconds())
            Truth.assertThat(updatedRanges?.get(1)).isEqualTo(endDate.toEpochMilliseconds())
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

    @Test
    fun customRangeShouldEndAtTheLastMillisecondOfThePickedDay() = runTest {
        // The picker saves both bounds as midnight of the picked day; reading the range back
        // must return the whole end day, so BETWEEN queries keep it inside the filter.
        val start = localMidnight(2026, 11, 15)
        val end = localMidnight(2026, 11, 22)
        repository.setDateRanges(listOf(Instant.fromEpochMilliseconds(start), Instant.fromEpochMilliseconds(end)))

        val model = repository.getDateRangeFilterTypeString(DateRangeType.CUSTOM)

        Truth.assertThat(model.type).isEqualTo(DateRangeType.CUSTOM)
        Truth.assertThat(model.dateRanges[0]).isEqualTo(start)
        val savedEnd = Instant.fromEpochMilliseconds(model.dateRanges[1]).toLocalDateTime(tz)
        Truth.assertThat(savedEnd.date).isEqualTo(LocalDate(2026, 11, 22))
        Truth.assertThat(savedEnd.hour).isEqualTo(23)
    }

    @Test
    fun nonCustomRangeShouldKeepItsEndBoundUntouched() = runTest {
        val start = localMidnight(2026, 11, 1)
        val end = localMidnight(2026, 12, 1)
        repository.setDateRanges(listOf(Instant.fromEpochMilliseconds(start), Instant.fromEpochMilliseconds(end)))

        val model = repository.getDateRangeFilterTypeString(DateRangeType.THIS_MONTH)
        // A navigated non-custom range reuses the stored window as-is (no end-day extension).
        Truth.assertThat(model.dateRanges[0]).isEqualTo(start)
        Truth.assertThat(model.dateRanges[1]).isEqualTo(end)
    }

    private fun localMidnight(year: Int, month: Int, day: Int): Long = LocalDate(year, month, day).atStartOfDayIn(tz).toEpochMilliseconds()

    private val tz = TimeZone.currentSystemDefault()
}
