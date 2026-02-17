package com.naveenapps.expensemanager.feature.transaction.list.new

import androidx.compose.ui.graphics.Color

// ── Colour palette (dark fintech theme) ────────────────────────────────
object AppColors {
    val Surface       = Color(0xFF0E1117)
    val Card          = Color(0xFF161B26)
    val CardBorder    = Color(0xFF1E2330)
    val ActiveTab     = Color(0xFF1E2740)
    val TextPrimary   = Color(0xFFF0F2F5)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextTertiary  = Color(0xFF6B7280)
    val TextQuaternary= Color(0xFF4B5563)
    val Accent        = Color(0xFF5B8DEF)
    val AccentDark    = Color(0xFF4A7ADB)
    val Income        = Color(0xFF4CAF7D)
    val Expense       = Color(0xFFE8634A)
    val Divider       = Color(0xFF1E2330)
    val SearchBg      = Color(0xFF161B26)
}

// ── Category definition ────────────────────────────────────────────────
data class TransactionCategory(
    val key: String,
    val label: String,
    val icon: String,
    val color: Color,
)

val Categories = listOf(
    TransactionCategory("food",          "Food & Dining",    "🍕", Color(0xFFE8634A)),
    TransactionCategory("transport",     "Transport",        "🚗", Color(0xFF5B8DEF)),
    TransactionCategory("shopping",      "Shopping",         "🛍️", Color(0xFFD4A843)),
    TransactionCategory("entertainment", "Entertainment",    "🎬", Color(0xFF9B6DD7)),
    TransactionCategory("bills",         "Bills & Utilities","💡", Color(0xFF45B8AC)),
    TransactionCategory("health",        "Health",           "💊", Color(0xFFEF6B8A)),
    TransactionCategory("salary",        "Salary",           "💼", Color(0xFF4CAF7D)),
    TransactionCategory("freelance",     "Freelance",        "💻", Color(0xFF6DBFB8)),
    TransactionCategory("investment",    "Investment",       "📈", Color(0xFF7B9E42)),
    TransactionCategory("groceries",     "Groceries",        "🥦", Color(0xFF8ABB5E)),
)

val CategoryMap = Categories.associateBy { it.key }

// ── Account definition ─────────────────────────────────────────────────
data class Account(
    val key: String,
    val name: String,
    val icon: String,
)

val Accounts = listOf(
    Account("checking", "Checking",    "🏦"),
    Account("savings",  "Savings",     "🐷"),
    Account("cash",     "Cash",        "💵"),
    Account("credit",   "Credit Card", "💳"),
)

val AccountMap = Accounts.associateBy { it.key }

// ── Transaction type ───────────────────────────────────────────────────
enum class TransactionType { INCOME, EXPENSE }

// ── Transaction model ──────────────────────────────────────────────────
data class Transaction(
    val id: Int,
    val title: String,
    val categoryKey: String,
    val accountKey: String,
    val amount: Double,
    val date: String,      // yyyy-MM-dd
    val time: String,      // h:mm a
    val type: TransactionType,
) {
    val category get() = CategoryMap[categoryKey]!!
    val account  get() = AccountMap[accountKey]!!
}

// ── Filter tab options ─────────────────────────────────────────────────
enum class TypeFilter(val label: String) {
    ALL("All"), EXPENSE("Expense"), INCOME("Income")
}

// ── Sample data ────────────────────────────────────────────────────────
val SampleTransactions = listOf(
    Transaction(1,  "Whole Foods Market",  "groceries",     "checking", -87.34,  "2026-02-17", "10:24 AM", TransactionType.EXPENSE),
    Transaction(2,  "Monthly Salary",      "salary",        "checking",  5200.0, "2026-02-17", "9:00 AM",  TransactionType.INCOME),
    Transaction(3,  "Uber Ride",           "transport",     "credit",   -23.50,  "2026-02-16", "7:45 PM",  TransactionType.EXPENSE),
    Transaction(4,  "Netflix Subscription","entertainment", "credit",   -15.99,  "2026-02-16", "12:00 PM", TransactionType.EXPENSE),
    Transaction(5,  "Chipotle",            "food",          "cash",     -14.25,  "2026-02-16", "1:15 PM",  TransactionType.EXPENSE),
    Transaction(6,  "Freelance Project",   "freelance",     "checking",  850.0,  "2026-02-15", "3:30 PM",  TransactionType.INCOME),
    Transaction(7,  "Electric Bill",       "bills",         "checking", -142.0,  "2026-02-15", "8:00 AM",  TransactionType.EXPENSE),
    Transaction(8,  "Amazon Purchase",     "shopping",      "credit",   -59.99,  "2026-02-15", "11:30 AM", TransactionType.EXPENSE),
    Transaction(9,  "Pharmacy",            "health",        "cash",     -28.50,  "2026-02-14", "4:20 PM",  TransactionType.EXPENSE),
    Transaction(10, "Dividend Payment",    "investment",    "savings",   32.18,  "2026-02-14", "9:00 AM",  TransactionType.INCOME),
    Transaction(11, "Starbucks",           "food",          "credit",    -6.75,  "2026-02-14", "8:15 AM",  TransactionType.EXPENSE),
    Transaction(12, "Gas Station",         "transport",     "checking", -48.20,  "2026-02-13", "6:30 PM",  TransactionType.EXPENSE),
    Transaction(13, "Gym Membership",      "health",        "checking", -49.99,  "2026-02-13", "10:00 AM", TransactionType.EXPENSE),
    Transaction(14, "Target",              "shopping",      "credit",   -73.44,  "2026-02-13", "2:45 PM",  TransactionType.EXPENSE),
    Transaction(15, "Water Bill",          "bills",         "checking", -55.00,  "2026-02-12", "8:00 AM",  TransactionType.EXPENSE),
    Transaction(16, "Sushi Restaurant",    "food",          "cash",     -42.80,  "2026-02-12", "7:00 PM",  TransactionType.EXPENSE),
)
