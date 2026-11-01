package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_BUDGET
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * A budget must cover exactly the user's local month (or year) wherever the phone is and whatever
 * language it's set to. The same checks run for every app language plus the time zones most likely
 * to break date maths: half-hour and 45-minute offsets, daylight-saving changes near month ends, both
 * sides of the date line, non-Latin digits, and non-Gregorian default calendars (Thai Buddhist,
 * Japanese imperial).
 *
 * Expected values are computed independently with java.time, which always uses the ISO calendar.
 * The repository is stubbed to filter like the real SQL query (`created_on BETWEEN start AND end`),
 * so the edge-of-month tests exercise the whole path: budget key → date range → query → key filter.
 *
 * Parameters run one after another on the same thread, so these tests also catch date formatters
 * that keep a stale time zone after the device zone changes.
 */
@RunWith(Parameterized::class)
class GetBudgetTransactionsUseCaseTimeZoneLocaleTest(
    private val zoneId: String,
    private val localeTag: String,
) : BaseCoroutineTest() {

    private val categoryRepository: CategoryRepository = mock()
    private val accountRepository: AccountRepository = mock()
    private val transactionRepository: TransactionRepository = mock()

    private lateinit var useCase: GetBudgetTransactionsUseCase
    private lateinit var originalTimeZone: TimeZone
    private lateinit var originalLocale: Locale

    private val zone: ZoneId get() = ZoneId.of(zoneId)

    override fun onCreate() {
        super.onCreate()
        originalTimeZone = TimeZone.getDefault()
        originalLocale = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
        Locale.setDefault(Locale.forLanguageTag(localeTag))

        whenever(categoryRepository.getCategories()).thenReturn(flowOf(listOf(FAKE_CATEGORY)))
        whenever(accountRepository.getAccounts()).thenReturn(flowOf(listOf(FAKE_ACCOUNT)))
        useCase = GetBudgetTransactionsUseCase(categoryRepository, accountRepository, transactionRepository)
    }

    override fun onDestroy() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
        super.onDestroy()
    }

    @Test
    fun runsInTheRequestedTimeZone() {
        // TimeZone.getTimeZone() silently falls back to GMT for a mistyped ID.
        Truth.assertThat(TimeZone.getDefault().id).isEqualTo(zoneId)
    }

    @Test
    fun budgetKeyIsEnglishAndGregorianWhateverTheLocale() {
        Truth.assertThat(Date(localMillis(2026, 10, 15, 12, 0)).toMonthAndYearKey()).isEqualTo("October 2026")
        Truth.assertThat(Date(localMillis(2028, 2, 15, 12, 0)).toMonthAndYearKey()).isEqualTo("February 2028")
    }

    @Test
    fun monthlyRangeIsExactlyTheLocalMonth() = runTest {
        whenever(
            transactionRepository.getFilteredTransaction(any(), any(), any(), any(), any()),
        ).thenReturn(flowOf(listOf()))

        MONTHS.forEach { (year, month) -> useCase.invoke(monthlyBudget(year, month)) }

        val start = argumentCaptor<Long>()
        val end = argumentCaptor<Long>()
        verify(transactionRepository, times(MONTHS.size))
            .getFilteredTransaction(any(), any(), any(), start.capture(), end.capture())

        MONTHS.forEachIndexed { index, (year, month) ->
            val firstDay = LocalDate.of(year, month, 1)
            val label = "$year-$month in $zoneId / $localeTag"
            Truth.assertWithMessage("start of $label").that(start.allValues[index])
                .isEqualTo(startOfDay(firstDay))
            Truth.assertWithMessage("end of $label").that(end.allValues[index])
                .isEqualTo(startOfDay(firstDay.plusMonths(1)) - 1)
        }
    }

    @Test
    fun firstAndLastMinuteOfOctoberCountAndNeighbouringDaysDoNot() = runTest {
        stubQueryLikeSqlBetween(
            transactionAt("sep-30-23:59:59", localMillis(2026, 9, 30, 23, 59, 59)),
            transactionAt("oct-01-00:00", localMillis(2026, 10, 1, 0, 0)),
            transactionAt("oct-30-12:00", localMillis(2026, 10, 30, 12, 0)),
            transactionAt("oct-31-23:59:59", localMillis(2026, 10, 31, 23, 59, 59)),
            transactionAt("nov-01-00:00", localMillis(2026, 11, 1, 0, 0)),
        )

        val ids = transactionIds(useCase.invoke(monthlyBudget(2026, 10)))

        Truth.assertThat(ids).containsExactly("oct-01-00:00", "oct-30-12:00", "oct-31-23:59:59")
    }

    @Test
    fun lastFourDaysOfMarchCount() = runTest {
        // March lost 28–31 under the old range, and contains the spring DST change in Europe and the US.
        stubQueryLikeSqlBetween(
            transactionAt("mar-28-09:00", localMillis(2026, 3, 28, 9, 0)),
            transactionAt("mar-29-12:00", localMillis(2026, 3, 29, 12, 0)),
            transactionAt("mar-31-23:59:59", localMillis(2026, 3, 31, 23, 59, 59)),
            transactionAt("apr-01-00:00", localMillis(2026, 4, 1, 0, 0)),
        )

        val ids = transactionIds(useCase.invoke(monthlyBudget(2026, 3)))

        Truth.assertThat(ids).containsExactly("mar-28-09:00", "mar-29-12:00", "mar-31-23:59:59")
    }

    @Test
    fun leapDayCountsInFebruary() = runTest {
        stubQueryLikeSqlBetween(
            transactionAt("feb-29-20:00", localMillis(2028, 2, 29, 20, 0)),
            transactionAt("mar-01-00:00", localMillis(2028, 3, 1, 0, 0)),
        )

        val ids = transactionIds(useCase.invoke(monthlyBudget(2028, 2)))

        Truth.assertThat(ids).containsExactly("feb-29-20:00")
    }

    @Test
    fun yearlyBudgetCoversFirstAndLastMinuteOfTheYear() = runTest {
        stubQueryLikeSqlBetween(
            transactionAt("2025-12-31-23:59:59", localMillis(2025, 12, 31, 23, 59, 59)),
            transactionAt("2026-01-01-00:00", localMillis(2026, 1, 1, 0, 0)),
            transactionAt("2026-12-31-23:59:59", localMillis(2026, 12, 31, 23, 59, 59)),
            transactionAt("2027-01-01-00:00", localMillis(2027, 1, 1, 0, 0)),
        )
        val budget = FAKE_BUDGET.copy(selectedMonth = "2026", periodType = BudgetPeriod.YEARLY)

        val ids = transactionIds(useCase.invoke(budget))

        Truth.assertThat(ids).containsExactly("2026-01-01-00:00", "2026-12-31-23:59:59")
    }

    /** Builds the budget the way the app does: the key comes from a date inside the month. */
    private fun monthlyBudget(year: Int, month: Int): Budget = FAKE_BUDGET.copy(selectedMonth = Date(localMillis(year, month, 15, 12, 0)).toMonthAndYearKey())

    /** Mirrors the DAO's `created_on BETWEEN :fromDate AND :toDate` (both ends inclusive). */
    private fun stubQueryLikeSqlBetween(vararg transactions: Transaction) {
        whenever(
            transactionRepository.getFilteredTransaction(any(), any(), any(), any(), any()),
        ).thenAnswer { invocation ->
            val start = invocation.getArgument<Long>(3)
            val end = invocation.getArgument<Long>(4)
            flowOf(transactions.filter { it.createdOn.time in start..end })
        }
    }

    private fun transactionIds(response: Resource<List<Transaction>>): List<String> {
        Truth.assertThat(response).isInstanceOf(Resource.Success::class.java)
        return (response as Resource.Success).data.map { it.id }
    }

    private fun transactionAt(id: String, millis: Long): Transaction = FAKE_EXPENSE_TRANSACTION.copy(id = id, createdOn = Date(millis), updatedOn = Date(millis))

    private fun localMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): Long = LocalDateTime.of(year, month, day, hour, minute, second).atZone(zone).toInstant().toEpochMilli()

    private fun startOfDay(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    companion object {
        /** 31-day, 30-day and February months, months with DST changes, and a leap February. */
        private val MONTHS = listOf(
            2026 to 1,
            2026 to 2,
            2026 to 3,
            2026 to 4,
            2026 to 9,
            2026 to 10,
            2026 to 11,
            2026 to 12,
            2028 to 2,
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0} / {1}")
        fun parameters(): List<Array<Any>> = listOf(
            // Every app language, in a time zone where it's used.
            arrayOf("Asia/Kolkata", "en-IN"), // largest user base, +05:30
            arrayOf("Asia/Kolkata", "hi-IN"),
            arrayOf("Asia/Riyadh", "ar-SA"), // Arabic-Indic digits by default
            arrayOf("Africa/Cairo", "ar-EG"), // DST ends on the last Thursday of October
            arrayOf("Asia/Shanghai", "zh-CN"),
            arrayOf("Asia/Tokyo", "ja-JP-u-ca-japanese"), // Japanese imperial calendar
            arrayOf("Asia/Seoul", "ko-KR"),
            arrayOf("Europe/Berlin", "de-DE"), // DST: 29 March and 25 October 2026
            arrayOf("Europe/Paris", "fr-FR"),
            arrayOf("Europe/Madrid", "es-ES"),
            arrayOf("Europe/Rome", "it-IT"),
            arrayOf("Europe/Warsaw", "pl-PL"),
            arrayOf("America/Sao_Paulo", "pt-BR"),
            arrayOf("Europe/Moscow", "ru-RU"),
            arrayOf("Europe/Istanbul", "tr-TR"), // Turkish dotted/dotless i casing
            arrayOf("Asia/Jakarta", "id-ID"),
            arrayOf("Asia/Ho_Chi_Minh", "vi-VN"),
            arrayOf("America/New_York", "en-US"), // DST: 8 March and 1 November 2026
            // Other countries from the user base and awkward offsets.
            arrayOf("Asia/Karachi", "ur-PK"),
            arrayOf("Asia/Manila", "en-PH"),
            arrayOf("Asia/Bangkok", "th-TH"), // Buddhist calendar by default
            arrayOf("Asia/Kathmandu", "en-US"), // +05:45
            arrayOf("Asia/Tehran", "fa-IR"), // +03:30, Persian digits
            arrayOf("Pacific/Auckland", "en-NZ"), // +13:00 in summer
            arrayOf("Pacific/Kiritimati", "en-US"), // +14:00
            arrayOf("Pacific/Pago_Pago", "en-US"), // -11:00
            arrayOf("UTC", "en-GB"),
        )
    }
}
