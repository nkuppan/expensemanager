package com.naveenapps.expensemanager.demo

import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

/**
 * DEBUG BUILDS ONLY — realistic, repeatable sample data for Play Store screenshots.
 *
 * Pure Kotlin (no Android, no database) so it can be unit tested. Amounts are in US dollars;
 * [DemoCurrency] converts them to "nice" amounts in the currency being shown.
 * Category ids match BASE_CATEGORY_LIST in PreloadDatabaseInitializer.
 */
object DemoCategory {
    const val CLOTHING = "1"
    const val ENTERTAINMENT = "2"
    const val FOOD = "3"
    const val HEALTH = "4"
    const val LEISURE = "5"
    const val SHOPPING = "6"
    const val TRANSPORTATION = "7"
    const val UTILITIES = "8"
    const val SALARY = "9"
    const val GIFT = "10"
    const val COUPONS = "11"
}

object DemoAccountId {
    const val BANK = "demo-bank"
    const val CARD = "demo-card"
    const val CASH = "demo-cash"
}

enum class DemoAccountKind { BANK, CARD, CASH }

data class DemoAccount(
    val id: String,
    val kind: DemoAccountKind,
    val type: AccountType,
    val iconName: String,
    val color: String,
    val openingUsd: Double,
    val creditLimitUsd: Double = 0.0,
)

data class DemoTransaction(
    val type: TransactionType,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val amountUsd: Double,
    val date: Date,
    /** Set on the latest occurrence of a repeating bill/income, which becomes a recurring rule. */
    val repeat: RecurringFrequency? = null,
)

data class DemoBudget(
    val month: Date,
    val amountUsd: Double,
    /** Empty = all categories. */
    val categoryIds: List<String>,
)

data class DemoPlan(
    val accounts: List<DemoAccount>,
    /** Oldest first, so account balances are applied in order. */
    val transactions: List<DemoTransaction>,
    val budgets: List<DemoBudget>,
)

object DemoDataGenerator {

    val ACCOUNTS = listOf(
        DemoAccount(DemoAccountId.BANK, DemoAccountKind.BANK, AccountType.REGULAR, "account_balance", "#1E88E5", openingUsd = 3_150.0),
        DemoAccount(DemoAccountId.CARD, DemoAccountKind.CARD, AccountType.CREDIT, "credit_card", "#8E24AA", openingUsd = 0.0, creditLimitUsd = 3_000.0),
        DemoAccount(DemoAccountId.CASH, DemoAccountKind.CASH, AccountType.REGULAR, "savings", "#43A047", openingUsd = 260.0),
    )

    /**
     * @param today the last day that may get transactions (nothing is dated in the future).
     * @param months how many calendar months to fill, ending with the current one.
     */
    fun generate(today: Date = Date(), months: Int = 3, seed: Int = 42): DemoPlan {
        val todayCal = Calendar.getInstance().apply { time = today }
        val transactions = mutableListOf<DemoTransaction>()
        val budgets = mutableListOf<DemoBudget>()

        for (offset in (months - 1) downTo 0) {
            val monthStart = (todayCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -offset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val isCurrentMonth = offset == 0
            val lastDay = if (isCurrentMonth) todayCal.get(Calendar.DAY_OF_MONTH) else monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
            val random = Random(seed * 31 + offset)
            transactions += month(monthStart, lastDay, isCurrentMonth, offset, random, todayCal)

            budgets += DemoBudget(monthStart.time, amountUsd = 1_800.0, categoryIds = emptyList())
            budgets += DemoBudget(monthStart.time, amountUsd = 650.0, categoryIds = listOf(DemoCategory.FOOD))
            budgets += DemoBudget(
                monthStart.time,
                amountUsd = 200.0,
                categoryIds = listOf(DemoCategory.ENTERTAINMENT, DemoCategory.LEISURE),
            )
        }

        transactions.sortBy { it.date }
        return DemoPlan(ACCOUNTS, markLatestRepeats(transactions), budgets)
    }

    private fun month(
        monthStart: Calendar,
        lastDay: Int,
        isCurrentMonth: Boolean,
        monthsAgo: Int,
        random: Random,
        today: Calendar,
    ): List<DemoTransaction> {
        val out = mutableListOf<DemoTransaction>()

        fun at(day: Int, hour: Int = random.nextInt(8, 21)): Date? {
            if (day > lastDay) return null
            val c = (monthStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, random.nextInt(0, 60))
            }
            // Same-day entries must not be later than "now".
            if (isCurrentMonth && day == lastDay && c.after(today)) {
                c.timeInMillis = today.timeInMillis - random.nextLong(5, 120) * 60_000
            }
            return c.time
        }

        fun add(type: TransactionType, category: String, from: String, usd: Double, day: Int, to: String? = null, repeat: RecurringFrequency? = null, hour: Int? = null) {
            val date = (if (hour != null) at(day, hour) else at(day)) ?: return
            out += DemoTransaction(type, category, from, to, round2(usd), date, repeat)
        }

        fun range(min: Double, max: Double) = min + random.nextDouble() * (max - min)
        fun days(count: Int): List<Int> {
            val max = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH)
            return (1..max).shuffled(random).take(count).sorted()
        }

        val bank = DemoAccountId.BANK
        val card = DemoAccountId.CARD
        val cash = DemoAccountId.CASH

        // ── Income ──
        add(TransactionType.INCOME, DemoCategory.SALARY, bank, 3_100.0, day = 1, repeat = RecurringFrequency.MONTHLY, hour = 9)
        if (monthsAgo % 2 == 1) add(TransactionType.INCOME, DemoCategory.GIFT, cash, range(50.0, 150.0), days(1).first())
        days(1).forEach { add(TransactionType.INCOME, DemoCategory.COUPONS, card, range(5.0, 20.0), it) }

        // ── Bills ──
        add(TransactionType.EXPENSE, DemoCategory.UTILITIES, bank, range(62.0, 108.0), day = 3) // electricity
        add(TransactionType.EXPENSE, DemoCategory.UTILITIES, bank, 45.0, day = 5, repeat = RecurringFrequency.MONTHLY) // internet
        add(TransactionType.EXPENSE, DemoCategory.ENTERTAINMENT, card, 15.99, day = 8, repeat = RecurringFrequency.MONTHLY) // streaming
        add(TransactionType.EXPENSE, DemoCategory.HEALTH, card, 35.0, day = 10, repeat = RecurringFrequency.MONTHLY) // gym
        add(TransactionType.EXPENSE, DemoCategory.UTILITIES, bank, 30.0, day = 20) // phone

        // ── Moving money ──
        add(TransactionType.TRANSFER, DemoCategory.UTILITIES, bank, 850.0, day = 12, to = card) // card payment
        add(TransactionType.TRANSFER, DemoCategory.UTILITIES, bank, 300.0, day = 15, to = cash) // ATM

        // ── Everyday spending ──
        val groceryStart = 2 + random.nextInt(0, 5)
        for (day in groceryStart..31 step 7) add(TransactionType.EXPENSE, DemoCategory.FOOD, card, range(54.0, 118.0), day)
        days(5).forEach { add(TransactionType.EXPENSE, DemoCategory.FOOD, if (random.nextBoolean()) card else cash, range(18.0, 58.0), it, hour = random.nextInt(12, 22)) }
        days(6).forEach { add(TransactionType.EXPENSE, DemoCategory.FOOD, cash, range(3.5, 8.5), it, hour = random.nextInt(8, 11)) }
        days(8).forEach { add(TransactionType.EXPENSE, DemoCategory.TRANSPORTATION, if (random.nextInt(3) == 0) card else cash, range(6.0, 48.0), it) }
        days(random.nextInt(2, 4)).forEach { add(TransactionType.EXPENSE, DemoCategory.SHOPPING, card, range(24.0, 140.0), it) }
        days(random.nextInt(1, 3)).forEach { add(TransactionType.EXPENSE, DemoCategory.CLOTHING, card, range(29.0, 95.0), it) }
        days(random.nextInt(1, 3)).forEach { add(TransactionType.EXPENSE, DemoCategory.ENTERTAINMENT, card, range(14.0, 32.0), it, hour = random.nextInt(17, 22)) }
        days(1).forEach { add(TransactionType.EXPENSE, DemoCategory.HEALTH, cash, range(9.0, 35.0), it) }
        if (monthsAgo == 1) days(1).forEach { add(TransactionType.EXPENSE, DemoCategory.LEISURE, card, range(240.0, 420.0), it) } // weekend trip

        return out
    }

    /** The latest occurrence of each repeating item keeps its [DemoTransaction.repeat]; earlier ones drop it. */
    private fun markLatestRepeats(sorted: List<DemoTransaction>): List<DemoTransaction> {
        val latestIndex = mutableMapOf<String, Int>()
        sorted.forEachIndexed { i, t -> if (t.repeat != null) latestIndex[repeatKey(t)] = i }
        return sorted.mapIndexed { i, t -> if (t.repeat != null && latestIndex[repeatKey(t)] != i) t.copy(repeat = null) else t }
    }

    private fun repeatKey(t: DemoTransaction) = "${t.categoryId}|${t.fromAccountId}|${t.amountUsd}|${t.repeat}"

    private fun round2(v: Double) = Math.round(v * 100.0) / 100.0
}

/** Converts the USD sample amounts into believable amounts in another currency. */
object DemoCurrency {
    /** Rough rates, only so the numbers look normal for that country — not for anything financial. */
    private val USD_TO = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "INR" to 84.0, "JPY" to 150.0, "KRW" to 1_380.0,
        "CNY" to 7.2, "IDR" to 16_000.0, "VND" to 25_000.0, "RUB" to 92.0, "TRY" to 34.0, "PLN" to 4.0,
        "BRL" to 5.5, "SAR" to 3.75, "AED" to 3.67, "EGP" to 49.0, "MXN" to 18.0, "CAD" to 1.37,
        "AUD" to 1.52, "CHF" to 0.88,
    )

    fun convert(usd: Double, currencyCode: String, fractionDigits: Int): Double {
        val rate = USD_TO[currencyCode.uppercase()] ?: 1.0
        val value = usd * rate
        val step = when {
            rate >= 5_000 -> 1_000.0
            rate >= 500 -> 100.0
            rate >= 50 -> 10.0
            rate >= 5 -> 1.0
            fractionDigits == 0 -> 1.0
            else -> 0.01
        }
        val rounded = Math.round(value / step) * step
        return if (step < 1) Math.round(rounded * 100.0) / 100.0 else rounded
    }
}
