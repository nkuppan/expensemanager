package com.nkuppan.expensemanager.microbenchmark

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.data.db.ExpenseManagerDatabase
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseBenchmarkTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var accountDao: AccountDao

    @Before
    fun start() {

        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            ExpenseManagerDatabase::class.java,
            "sample.db"
        ).build()

        accountDao = db.accountDao()
    }

    @Test
    fun accountInsertTest() {
        benchmarkRule.measureRepeated {
            accountDao.insert(getRandomAccount())
        }
    }

    companion object {
        private fun getRandomAccount() = AccountEntity(
            id = UUID.randomUUID().toString(),
            name = "Fake Account",
            type = PaymentMode.WALLET.ordinal,
            backgroundColor = "#000000",
            createdOn = Date(),
            updatedOn = Date()
        )
    }
}