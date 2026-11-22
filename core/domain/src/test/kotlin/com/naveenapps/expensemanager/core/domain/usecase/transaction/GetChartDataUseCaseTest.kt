package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

/**
 * Chart building for every date-range type, against the exact range bounds the app stores:
 * built-in ranges end at the start of the next period, the custom picker stores local
 * midnight of each picked day.
 */
class GetChartDataUseCaseTest {

    private val tz = TimeZone.currentSystemDefault()

    private fun localMidnight(year: Int, month: Int, day: Int): Long = LocalDate(year, month, day).atStartOfDayIn(tz).toEpochMilliseconds()

    private fun tx(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        amount: Double = 100.0,
        type: TransactionType = TransactionType.EXPENSE,
    ): Transaction {
        val epoch = LocalDateTime(year, month, day, hour, 0).toInstant(tz).toEpochMilliseconds()
        return Transaction(
            id = "$year-$month-$day-$hour-$type",
            notes = "",
            categoryId = "1",
            fromAccountId = "1",
            toAccountId = null,
            type = type,
            amount = Amount(amount),
            imagePath = "",
            createdOn = Date(epoch),
            updatedOn = Date(epoch),
            category = Category(
                id = "1",
                name = "Test",
                type = if (type == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME,
                storedIcon = StoredIcon("test", "#FFFFFF"),
                createdOn = Date(epoch),
                updatedOn = Date(epoch),
            ),
        )
    }

    private fun useCase(
        type: DateRangeType,
        range: List<Long>,
        transactions: List<Transaction>,
        groupType: GroupType = GroupType.DATE,
    ): GetChartDataUseCase {
        val currency = Currency(symbol = "$", name = "USD")

        val getCurrencyUseCase: GetCurrencyUseCase = mock()
        whenever(getCurrencyUseCase.invoke()).thenReturn(flowOf(currency))

        val getFormattedAmountUseCase: GetFormattedAmountUseCase = mock()
        whenever(getFormattedAmountUseCase.invoke(any(), any())).thenAnswer {
            Amount(it.getArgument<Double>(0), "x")
        }

        val getDateRangeUseCase: GetDateRangeUseCase = mock()
        whenever(getDateRangeUseCase.invoke()).thenReturn(
            flowOf(DateRangeModel("range", "desc", type, range)),
        )

        val getTransactionGroupTypeUseCase: GetTransactionGroupTypeUseCase = mock()
        wheneverBlocking { getTransactionGroupTypeUseCase.invoke(any()) }.thenReturn(groupType)

        val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase = mock()
        whenever(getTransactionWithFilterUseCase.invoke()).thenReturn(flowOf(transactions))

        return GetChartDataUseCase(
            getCurrencyUseCase = getCurrencyUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            getDateRangeUseCase = getDateRangeUseCase,
            getTransactionGroupTypeUseCase = getTransactionGroupTypeUseCase,
            getTransactionWithFilterUseCase = getTransactionWithFilterUseCase,
            dispatcher = AppCoroutineDispatchers(
                main = Dispatchers.Unconfined,
                io = Dispatchers.Unconfined,
                computation = Dispatchers.Unconfined,
            ),
        )
    }

    @Test
    fun `custom range within one month charts every day with the right totals`() = runTest {
        val result = useCase(
            type = DateRangeType.CUSTOM,
            range = listOf(localMidnight(2026, 11, 1), localMidnight(2026, 11, 22)),
            transactions = listOf(
                tx(2026, 11, 5, 10),
                tx(2026, 11, 15, 14),
                tx(2026, 11, 21, 9),
            ),
        ).invoke().first()

        val chart = result.chartData
        assertThat(chart).isNotNull()
        val expenses = chart!!.chartData[0]
        // One bucket per day from the first transaction's day through the range end.
        assertThat(chart.dates).hasSize(18)
        assertThat(expenses.sumOf { it.total }).isEqualTo(300.0)
    }

    @Test
    fun `single-day custom range with a midday transaction charts one point`() = runTest {
        // The picker stores both bounds as midnight of the same day; a transaction created
        // later that day must still land in the chart bucket.
        val day = localMidnight(2026, 11, 15)
        val result = useCase(
            type = DateRangeType.CUSTOM,
            range = listOf(day, day),
            transactions = listOf(tx(2026, 11, 15, 14)),
        ).invoke().first()

        val chart = result.chartData
        assertThat(chart).isNotNull()
        assertThat(chart!!.chartData[0]).hasSize(1)
        assertThat(chart.chartData[0].single().total).isEqualTo(100.0)
    }

    @Test
    fun `custom range with transactions only on its last day charts them`() = runTest {
        // The end bound is midnight of the last day; the earliest (and only) in-range
        // transaction happens later that day, which must not empty the chart.
        val result = useCase(
            type = DateRangeType.CUSTOM,
            range = listOf(localMidnight(2026, 11, 1), localMidnight(2026, 11, 22)),
            transactions = listOf(tx(2026, 11, 22, 14)),
        ).invoke().first()

        val chart = result.chartData
        assertThat(chart).isNotNull()
        assertThat(chart!!.chartData[0]).hasSize(1)
        assertThat(chart.chartData[0].single().total).isEqualTo(100.0)
    }

    @Test
    fun `custom range crossing months groups by month`() = runTest {
        val result = useCase(
            type = DateRangeType.CUSTOM,
            range = listOf(localMidnight(2026, 10, 1), localMidnight(2026, 11, 22)),
            transactions = listOf(
                tx(2026, 10, 10, 10),
                tx(2026, 11, 10, 14),
            ),
            groupType = GroupType.MONTH,
        ).invoke().first()

        val chart = result.chartData
        assertThat(chart).isNotNull()
        assertThat(chart!!.chartData[0]).hasSize(2)
        assertThat(chart.chartData[0].sumOf { it.total }).isEqualTo(200.0)
    }

    @Test
    fun `this month range keeps charting the same way`() = runTest {
        // Built-in ranges end at the start of the next period; the chart must stay
        // unchanged there (regression pin for the shared clamp code).
        val result = useCase(
            type = DateRangeType.THIS_MONTH,
            range = listOf(localMidnight(2026, 11, 1), localMidnight(2026, 12, 1)),
            transactions = listOf(
                tx(2026, 11, 5, 10),
                tx(2026, 11, 21, 9),
            ),
        ).invoke().first()

        val chart = result.chartData
        assertThat(chart).isNotNull()
        assertThat(chart!!.chartData[0].sumOf { it.total }).isEqualTo(200.0)
        // Starts at the earliest transaction's day (no empty leading buckets) and keeps the
        // empty trailing buckets up to the exclusive end bound, as before the fix.
        assertThat(chart.dates.first()).isEqualTo("05/11/2026")
        assertThat(chart.dates).contains("30/11/2026")
    }

    @Test
    fun `empty period yields no chart data`() = runTest {
        val result = useCase(
            type = DateRangeType.CUSTOM,
            range = listOf(localMidnight(2026, 11, 1), localMidnight(2026, 11, 22)),
            transactions = emptyList(),
        ).invoke().first()

        assertThat(result.chartData).isNull()
        assertThat(result.transactions).isEmpty()
    }
}
