package com.naveenapps.expensemanager.core.data.repository

import androidx.room.Room
import androidx.room.useReaderConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.entity.RecurringTransactionEntity
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone as JavaTimeZone
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instants through Room end to end (KMP groundwork C4): entities hold kotlin.time.Instant and
 * DateConverter must still write exactly what the java.util.Date version wrote, the device's
 * local wall-clock time stored as if it were UTC. Expected column values are computed here with
 * java.time, independently of the app's code. Runs in India (UTC+5:30) so the difference
 * between "local as UTC" and a plain timestamp is visible.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class InstantStorageTest : BaseCoroutineTest() {

    private lateinit var database: ExpenseManagerDatabase
    private lateinit var originalZone: JavaTimeZone

    private val india = ZoneId.of("Asia/Kolkata")

    override fun onCreate() {
        super.onCreate()
        originalZone = JavaTimeZone.getDefault()
        JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(india))
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java,
        ).setDriver(AndroidSQLiteDriver()).allowMainThreadQueries().build()
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
        JavaTimeZone.setDefault(originalZone)
    }

    /** That wall-clock time in India, as an Instant. */
    private fun indiaTime(local: LocalDateTime): Instant = Instant.fromEpochMilliseconds(local.atZone(india).toInstant().toEpochMilli())

    /** What the database column must hold for [local]: the same wall-clock time, read as UTC. */
    private fun storedValue(local: LocalDateTime): Long = local.toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun rule(id: String, nextDueDate: Instant, createdOn: Instant = nextDueDate) = RecurringTransactionEntity(
        id = id,
        notes = "rent",
        categoryId = FAKE_CATEGORY.id,
        fromAccountId = FAKE_ACCOUNT.id,
        toAccountId = null,
        type = TransactionType.EXPENSE,
        amount = 12_000.0,
        frequency = "MONTHLY",
        startDate = createdOn,
        nextOccurrence = 1,
        nextDueDate = nextDueDate,
        createdOn = createdOn,
        updatedOn = createdOn,
    )

    private suspend fun insertParents() {
        database.accountDao().insert(FAKE_ACCOUNT.toEntityModel())
        database.categoryDao().insert(FAKE_CATEGORY.toEntityModel())
    }

    private fun column(name: String, id: String): Long = runBlocking {
        database.useReaderConnection { transactor ->
            transactor.usePrepared("SELECT $name FROM recurring_transaction WHERE id = ?") { statement ->
                statement.bindText(1, id)
                statement.step()
                statement.getLong(0)
            }
        }
    }

    @Test
    fun storesTheLocalWallClockTimeExactlyAsBefore() = runTest {
        insertParents()
        val local = LocalDateTime.of(2026, 11, 23, 10, 15, 30, 123_000_000)
        database.recurringTransactionDao().insert(rule("1", indiaTime(local)))

        listOf("start_date", "next_due_date", "created_on", "updated_on").forEach { name ->
            assertThat(column(name, "1")).isEqualTo(storedValue(local))
        }
    }

    @Test
    fun readsBackTheSameInstant() = runTest {
        insertParents()
        val at = indiaTime(LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_000_000))
        database.recurringTransactionDao().insert(rule("1", at))

        val loaded = database.recurringTransactionDao().findById("1")!!
        assertThat(loaded.nextDueDate).isEqualTo(at)
        assertThat(loaded.createdOn).isEqualTo(at)
    }

    /**
     * The database keeps milliseconds. A fresh Clock.System.now() can carry more, so a reloaded
     * date isn't == the one in memory: compare toEpochMilliseconds() across that boundary.
     */
    @Test
    fun keepsMillisecondsOnly() = runTest {
        insertParents()
        val precise = Instant.fromEpochSeconds(1_795_000_000L, 123_456_789)
        database.recurringTransactionDao().insert(rule("1", precise))

        val loaded = database.recurringTransactionDao().findById("1")!!.nextDueDate
        assertThat(loaded).isNotEqualTo(precise)
        assertThat(loaded.toEpochMilliseconds()).isEqualTo(precise.toEpochMilliseconds())
    }

    @Test
    fun getDueComparesInstantsIncludingTheBoundary() = runTest {
        insertParents()
        val cutOff = indiaTime(LocalDateTime.of(2026, 11, 23, 23, 59, 59, 999_000_000))
        val dao = database.recurringTransactionDao()
        dao.insert(rule("before", Instant.fromEpochMilliseconds(cutOff.toEpochMilliseconds() - 1)))
        dao.insert(rule("at", cutOff))
        dao.insert(rule("after", Instant.fromEpochMilliseconds(cutOff.toEpochMilliseconds() + 1)))

        assertThat(dao.getDue(cutOff).map { it.id }).containsExactly("before", "at").inOrder()
    }

    @Test
    fun advanceStoresTheNewInstants() = runTest {
        insertParents()
        val dao = database.recurringTransactionDao()
        dao.insert(rule("1", indiaTime(LocalDateTime.of(2026, 11, 1, 9, 0))))
        val nextLocal = LocalDateTime.of(2026, 12, 1, 9, 0)
        val updatedLocal = LocalDateTime.of(2026, 11, 23, 18, 30)

        dao.advance("1", nextOccurrence = 2, nextDueDate = indiaTime(nextLocal), updatedOn = indiaTime(updatedLocal))

        val loaded = dao.findById("1")!!
        assertThat(loaded.nextOccurrence).isEqualTo(2)
        assertThat(loaded.nextDueDate).isEqualTo(indiaTime(nextLocal))
        assertThat(column("next_due_date", "1")).isEqualTo(storedValue(nextLocal))
        assertThat(column("updated_on", "1")).isEqualTo(storedValue(updatedLocal))
    }

    /** "Floating" time: after travelling, a row still reads 10:15 on the same day, in the new zone. */
    @Test
    fun aRowKeepsItsWallClockTimeWhenTheDeviceChangesZone() = runTest {
        insertParents()
        val local = LocalDateTime.of(2026, 11, 23, 10, 15)
        database.recurringTransactionDao().insert(rule("1", indiaTime(local)))

        val newYork = ZoneId.of("America/New_York")
        JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(newYork))
        val loaded = database.recurringTransactionDao().findById("1")!!.nextDueDate

        assertThat(loaded.toEpochMilliseconds()).isEqualTo(local.atZone(newYork).toInstant().toEpochMilli())
    }
}
