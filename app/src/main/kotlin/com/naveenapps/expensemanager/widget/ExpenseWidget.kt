package com.naveenapps.expensemanager.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

internal fun fixedColor(color: Color): ColorProvider =
    androidx.glance.color.ColorProvider(day = color, night = color)

data class ExpenseWidgetData(
    val income: Double,
    val expenses: Double,
    val balanceStr: String,
    val incomeStr: String,
    val expensesStr: String,
    val changePercent: Double,
    val recentTransactions: List<WidgetTransaction>,
)

data class WidgetTransaction(
    val name: String,
    val iconName: String,
    val iconBgColor: String,
    val amountStr: String,
    val isIncome: Boolean,
)

object WidgetColors {
    val DarkBackground = Color(0xFF1A1F3C)
    val White = Color.White
    val WhiteDim = Color(0x66FFFFFF)
    val Green = Color(0xFF5DCAA5)
    val Red = Color(0xFFF0997B)
    val Purple = Color(0xFF7F77DD)
    val GreenBg = Color(0x265DCAA5)
    val PurpleBg = Color(0x267F77DD)
}

private fun iconResForName(name: String): Int = when (name) {
    "restaurant" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.restaurant
    "shopping_cart" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.shopping_cart
    "flight" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.flight
    "flights_and_hotels" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.flights_and_hotels
    "directions_car" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.directions_car
    "car_rental" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.car_rental
    "directions_bus" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.directions_bus
    "directions_bike" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.directions_bike
    "directions_boat" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.directions_boat
    "train" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.train
    "savings" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.savings
    "payments" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.payments
    "credit_card" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.credit_card
    "fitness_center" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.fitness_center
    "store" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.store
    "devices" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.devices
    "laptop_chromebook" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.laptop_chromebook
    "sports_esports" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.sports_esports
    "redeem" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.redeem
    "account_balance" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.account_balance
    "account_balance_wallet" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.account_balance_wallet
    "liquor" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.liquor
    "fluid_med" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.fluid_med
    "travel" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.travel
    "agriculture" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.agriculture
    "apartment" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.apartment
    "apparel" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.apparel
    "diversity" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.diversity
    "electric_rickshaw" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.electric_rickshaw
    "electric_scooter" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.electric_scooter
    "interactive_space" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.interactive_space
    "other_admission" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.other_admission
    "pool" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.pool
    "qr_code" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.qr_code
    "snowmobile" -> com.naveenapps.expensemanager.core.designsystem.R.drawable.snowmobile
    else -> com.naveenapps.expensemanager.core.designsystem.R.drawable.wallet
}

class ExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpenseWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                refreshExpenseWidgetData(context)
            } finally {
                pending?.finish()
            }
        }
    }
}

private data class WidgetRefreshData(
    val income: Double,
    val expenses: Double,
    val incomeStr: String,
    val expenseStr: String,
    val balanceStr: String,
    val recentStr: String,
)

internal suspend fun refreshExpenseWidgetData(context: Context) {
    val koin = runCatching { GlobalContext.get() }.getOrNull() ?: return

    val getCurrency = koin.get<GetCurrencyUseCase>()
    val getTransactions = koin.get<GetTransactionWithFilterUseCase>()
    val getDateRange = koin.get<GetDateRangeUseCase>()
    val formatAmount = koin.get<GetFormattedAmountUseCase>()

    combine(
        getCurrency.invoke(),
        getTransactions.invoke(),
        getDateRange.invoke(),
    ) { currency, transactions, _ ->
        val incomeValue = transactions
            ?.filter { it.type == TransactionType.INCOME }
            ?.sumOf { it.amount.amount } ?: 0.0
        val expenseValue = transactions
            ?.filter { it.type == TransactionType.EXPENSE }
            ?.sumOf { it.amount.amount } ?: 0.0

        val incomeStr = formatAmount.invoke(incomeValue, currency).amountString.orEmpty()
        val expenseStr = formatAmount.invoke(expenseValue, currency).amountString.orEmpty()
        val balanceStr = formatAmount.invoke(incomeValue - expenseValue, currency).amountString.orEmpty()

        // Serialize last 3 transactions;  = field sep,  = record sep
        val recentStr = (transactions ?: emptyList())
            .sortedByDescending { it.createdOn }
            .take(3)
            .joinToString("") { tx ->
                val displayName = tx.notes.ifBlank { tx.category.name }
                val amtStr = formatAmount.invoke(tx.amount.amount, currency).amountString.orEmpty()
                "${displayName}${tx.category.storedIcon.name}${tx.category.storedIcon.backgroundColor}${amtStr}${tx.type == TransactionType.INCOME}"
            }

        WidgetRefreshData(incomeValue, expenseValue, incomeStr, expenseStr, balanceStr, recentStr)
    }.take(1).collect { data ->
        GlanceAppWidgetManager(context)
            .getGlanceIds(ExpenseWidget::class.java)
            .forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().also {
                        it[ExpenseWidget.PREF_INCOME] = data.income.toString()
                        it[ExpenseWidget.PREF_EXPENSE] = data.expenses.toString()
                        it[ExpenseWidget.PREF_INCOME_STR] = data.incomeStr
                        it[ExpenseWidget.PREF_EXPENSE_STR] = data.expenseStr
                        it[ExpenseWidget.PREF_BALANCE_STR] = data.balanceStr
                        it[ExpenseWidget.PREF_RECENT_TXN] = data.recentStr
                    }
                }
                ExpenseWidget().update(context, id)
            }
    }
}

class RefreshExpenseWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        refreshExpenseWidgetData(context)
    }
}

class ExpenseWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            COMPACT,
            STANDARD,
            EXPANDED
        )
    )

    companion object {
        private val COMPACT = DpSize(180.dp, 80.dp)
        private val STANDARD = DpSize(180.dp, 180.dp)
        private val EXPANDED = DpSize(360.dp, 180.dp)

        val PREF_INCOME = stringPreferencesKey("expense_widget_income")
        val PREF_EXPENSE = stringPreferencesKey("expense_widget_expense")
        val PREF_INCOME_STR = stringPreferencesKey("expense_widget_income_str")
        val PREF_EXPENSE_STR = stringPreferencesKey("expense_widget_expense_str")
        val PREF_BALANCE_STR = stringPreferencesKey("expense_widget_balance_str")
        val PREF_RECENT_TXN = stringPreferencesKey("expense_widget_recent_txn")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val income = prefs[PREF_INCOME]?.toDoubleOrNull() ?: 0.0
            val expenses = prefs[PREF_EXPENSE]?.toDoubleOrNull() ?: 0.0
            val incomeStr = prefs[PREF_INCOME_STR].orEmpty()
            val expensesStr = prefs[PREF_EXPENSE_STR].orEmpty()
            val balanceStr = prefs[PREF_BALANCE_STR].orEmpty()
            val savingsRate = if (income > 0.0) {
                kotlin.math.round(((income - expenses) / income) * 1000.0) / 10.0
            } else 0.0
            val recentTxns = prefs[PREF_RECENT_TXN]
                ?.split("")
                ?.filter { it.isNotBlank() }
                ?.mapNotNull { record ->
                    val parts = record.split("")
                    if (parts.size >= 5) WidgetTransaction(
                        name = parts[0],
                        iconName = parts[1],
                        iconBgColor = parts[2],
                        amountStr = parts[3],
                        isIncome = parts[4].toBoolean(),
                    ) else null
                } ?: emptyList()

            val data = ExpenseWidgetData(
                income = income,
                expenses = expenses,
                balanceStr = balanceStr,
                incomeStr = incomeStr,
                expensesStr = expensesStr,
                changePercent = savingsRate,
                recentTransactions = recentTxns,
            )
            val size = LocalSize.current

            GlanceTheme {
                when {
                    size.width >= EXPANDED.width -> ExpandedWidget(data)
                    size.height >= STANDARD.height -> StandardWidget(data)
                    else -> CompactWidget(data)
                }
            }
        }
    }
}

@Composable
fun CompactWidget(data: ExpenseWidgetData) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.DarkBackground)
            .cornerRadius(20.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(actionRunCallback<RefreshExpenseWidgetAction>())
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(38.dp)
                    .background(WidgetColors.PurpleBg)
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(com.naveenapps.expensemanager.core.designsystem.R.drawable.wallet),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(fixedColor(WidgetColors.Purple))
                )
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Balance",
                    style = TextStyle(
                        color = fixedColor(WidgetColors.WhiteDim),
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = data.balanceStr,
                    style = TextStyle(
                        color = fixedColor(WidgetColors.White),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Box(
                modifier = GlanceModifier
                    .width(0.5.dp)
                    .height(36.dp)
                    .background(WidgetColors.WhiteDim)
            ) {}

            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Expenses",
                    style = TextStyle(
                        color = fixedColor(WidgetColors.WhiteDim),
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = data.expensesStr,
                    style = TextStyle(
                        color = fixedColor(WidgetColors.Red),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun StandardWidget(data: ExpenseWidgetData) {
    val spentPercent = if (data.income > 0.0) {
        ((data.expenses / data.income) * 100).toInt().coerceIn(0, 100)
    } else 0

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.DarkBackground)
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Total balance",
                    style = TextStyle(
                        color = fixedColor(WidgetColors.WhiteDim),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = data.balanceStr,
                    style = TextStyle(
                        color = fixedColor(WidgetColors.White),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier
                        .background(WidgetColors.GreenBg)
                        .cornerRadius(20.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${data.changePercent}% saved",
                        style = TextStyle(
                            color = fixedColor(WidgetColors.Green),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            Box(
                modifier = GlanceModifier
                    .size(34.dp)
                    .background(WidgetColors.PurpleBg)
                    .cornerRadius(10.dp)
                    .clickable(actionRunCallback<RefreshExpenseWidgetAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(com.naveenapps.expensemanager.R.drawable.ic_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.size(18.dp),
                    colorFilter = ColorFilter.tint(fixedColor(WidgetColors.Purple))
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            StatPill(
                label = "Income",
                value = data.incomeStr,
                valueColor = WidgetColors.Green,
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            StatPill(
                label = "Expenses",
                value = data.expensesStr,
                valueColor = WidgetColors.Red,
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        LinearProgressIndicator(
            progress = spentPercent / 100f,
            modifier = GlanceModifier.fillMaxWidth().height(5.dp).cornerRadius(99.dp),
            color = fixedColor(WidgetColors.Red),
            backgroundColor = fixedColor(Color(0xFF2A2F50))
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = "$spentPercent% of income spent",
                style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 10.sp),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = "${data.balanceStr} saved",
                style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF252B50))
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 10.sp)
        )
        Text(
            text = value,
            style = TextStyle(
                color = fixedColor(valueColor),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun ExpandedWidget(data: ExpenseWidgetData) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.DarkBackground)
            .cornerRadius(20.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
        ) {
            Text(
                text = "Total balance",
                style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 10.sp)
            )
            Text(
                text = data.balanceStr,
                style = TextStyle(
                    color = fixedColor(WidgetColors.White),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Row(
                modifier = GlanceModifier
                    .background(WidgetColors.GreenBg)
                    .cornerRadius(20.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${data.changePercent}% saved",
                    style = TextStyle(color = fixedColor(WidgetColors.Green), fontSize = 10.sp)
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                MiniStatBox(
                    label = "Income",
                    value = data.incomeStr,
                    valueColor = WidgetColors.Green,
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                MiniStatBox(
                    label = "Spent",
                    value = data.expensesStr,
                    valueColor = WidgetColors.Red,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(12.dp))
        Box(
            modifier = GlanceModifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(Color(0x33FFFFFF))
        ) {}
        Spacer(modifier = GlanceModifier.width(12.dp))

        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent",
                    style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 10.sp),
                    modifier = GlanceModifier.defaultWeight()
                )
                Box(
                    modifier = GlanceModifier
                        .size(22.dp)
                        .background(WidgetColors.PurpleBg)
                        .cornerRadius(6.dp)
                        .clickable(actionRunCallback<RefreshExpenseWidgetAction>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(com.naveenapps.expensemanager.R.drawable.ic_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.size(13.dp),
                        colorFilter = ColorFilter.tint(fixedColor(WidgetColors.Purple))
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(8.dp))

            if (data.recentTransactions.isEmpty()) {
                Text(
                    text = "No transactions",
                    style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 11.sp)
                )
            } else {
                data.recentTransactions.forEach { tx ->
                    TransactionRow(tx)
                    Spacer(modifier = GlanceModifier.height(7.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniStatBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF252B50))
            .cornerRadius(8.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(color = fixedColor(WidgetColors.WhiteDim), fontSize = 9.sp)
        )
        Text(
            text = value,
            style = TextStyle(
                color = fixedColor(valueColor),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun TransactionRow(tx: WidgetTransaction) {
    val iconRes = iconResForName(tx.iconName)
    val bgColor = try {
        Color(android.graphics.Color.parseColor(tx.iconBgColor))
    } catch (_: Exception) {
        Color(0xFF252B50)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .background(bgColor)
                .cornerRadius(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(14.dp),
                colorFilter = ColorFilter.tint(fixedColor(WidgetColors.White))
            )
        }
        Spacer(modifier = GlanceModifier.width(7.dp))
        Text(
            text = tx.name,
            style = TextStyle(color = fixedColor(WidgetColors.White), fontSize = 11.sp),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        Text(
            text = if (tx.isIncome) "+${tx.amountStr}" else tx.amountStr,
            style = TextStyle(
                color = fixedColor(if (tx.isIncome) WidgetColors.Green else WidgetColors.Red),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}