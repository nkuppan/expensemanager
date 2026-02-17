package com.naveenapps.expensemanager.feature.transaction.list.new

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs

// ── Date helpers ───────────────────────────────────────────────────────

private fun formatDateHeader(dateStr: String): String {
    val date  = LocalDate.parse(dateStr)
    val today = LocalDate.now()
    return when (date) {
        today            -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> {
            val dow   = date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.US)
            val month = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.US)
            "$dow, $month ${date.dayOfMonth}"
        }
    }
}

private fun groupByDate(txns: List<Transaction>): List<Pair<String, List<Transaction>>> =
    txns.groupBy { it.date }
        .entries
        .sortedByDescending { it.key }
        .map { it.key to it.value }

// ── Main Screen ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: List<Transaction> = SampleTransactions,
    onAddClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onEditClick: (Transaction) -> Unit = {},
    onDeleteClick: (Transaction) -> Unit = {},
) {
    var typeFilter by remember { mutableStateOf(TypeFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("all") }
    var showSearch by remember { mutableStateOf(false) }

    // ── Filtering logic ──
    val filtered = remember(transactions, typeFilter, searchQuery, selectedAccount) {
        transactions.filter { t ->
            val matchesType = when (typeFilter) {
                TypeFilter.ALL     -> true
                TypeFilter.EXPENSE -> t.type == TransactionType.EXPENSE
                TypeFilter.INCOME  -> t.type == TransactionType.INCOME
            }
            val matchesAccount = selectedAccount == "all" || t.accountKey == selectedAccount
            val matchesSearch = searchQuery.isBlank() ||
                    t.title.contains(searchQuery, ignoreCase = true) ||
                    t.category.label.contains(searchQuery, ignoreCase = true)
            matchesType && matchesAccount && matchesSearch
        }
    }

    val grouped = remember(filtered) { groupByDate(filtered) }

    val totalIncome  = remember(filtered) { filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } }
    val totalExpense = remember(filtered) { filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { abs(it.amount) } }

    val typeCounts = remember(transactions) {
        mapOf(
            TypeFilter.ALL     to transactions.size,
            TypeFilter.EXPENSE to transactions.count { it.type == TransactionType.EXPENSE },
            TypeFilter.INCOME  to transactions.count { it.type == TransactionType.INCOME },
        )
    }

    // ── Entrance animation ──
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(18.dp),
                containerColor = AppColors.Accent,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 16.dp,
                ),
                modifier = Modifier.size(56.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {

            // ──────────── HEADER ────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 },
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)) {
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Transactions",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = DmSans,
                                    color = AppColors.TextPrimary,
                                    letterSpacing = (-0.5).sp,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${filtered.size} transaction${if (filtered.size != 1) "s" else ""} this period",
                                    fontSize = 13.sp,
                                    fontFamily = DmSans,
                                    color = AppColors.TextTertiary,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Search toggle
                                IconButton(
                                    onClick = { showSearch = !showSearch },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            if (showSearch) AppColors.Accent.copy(0.3f) else AppColors.CardBorder,
                                            RoundedCornerShape(12.dp),
                                        )
                                        .background(
                                            if (showSearch) AppColors.ActiveTab else Color.White.copy(0.03f)
                                        ),
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (showSearch) AppColors.Accent else AppColors.TextSecondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                // Filter button
                                IconButton(
                                    onClick = { /* open advanced filters */ },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, AppColors.CardBorder, RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(0.03f)),
                                ) {
                                    Icon(
                                        Icons.Outlined.FilterList,
                                        contentDescription = "Filter",
                                        tint = AppColors.TextSecondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        // ── Collapsible search ──
                        CollapsibleSearchBar(
                            visible = showSearch,
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    }
                }
            }

            // ──────────── SUMMARY CARDS ────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { 40 },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryCard(
                            label = "Income",
                            amount = totalIncome,
                            accentColor = AppColors.Income,
                            gradientStart = Color(0xFF162A1F),
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            label = "Expenses",
                            amount = totalExpense,
                            accentColor = AppColors.Expense,
                            gradientStart = Color(0xFF2A1619),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ──────────── ACCOUNT PILLS ────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMillis = 150)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AccountPill(
                            icon = "📊",
                            name = "All Accounts",
                            isSelected = selectedAccount == "all",
                            onClick = { selectedAccount = "all" },
                        )
                        Accounts.forEach { acc ->
                            AccountPill(
                                icon = acc.icon,
                                name = acc.name,
                                isSelected = selectedAccount == acc.key,
                                onClick = { selectedAccount = acc.key },
                            )
                        }
                    }
                }
            }

            // ──────────── TYPE FILTER BAR ────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMillis = 200)),
                ) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TypeFilterBar(
                            selected = typeFilter,
                            counts = typeCounts,
                            onSelect = { typeFilter = it },
                        )
                    }
                }
            }

            // ──────────── TRANSACTION GROUPS ────────────
            if (grouped.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                grouped.forEachIndexed { groupIndex, (date, txns) ->

                    // Date header
                    item(key = "header_$date") {
                        val dayNet = txns.sumOf { it.amount }
                        val delayMs = 250 + groupIndex * 60

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(500, delayMillis = delayMs)) +
                                    slideInVertically(tween(500, delayMillis = delayMs)) { 50 },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 18.dp, bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatDateHeader(date),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = DmSans,
                                    color = AppColors.TextTertiary,
                                    letterSpacing = 0.3.sp,
                                )
                                Text(
                                    text = "${if (dayNet >= 0) "+" else "−"}$${"%,.2f".format(abs(dayNet))}",
                                    fontSize = 12.sp,
                                    fontFamily = SpaceMono,
                                    color = AppColors.TextQuaternary,
                                )
                            }
                        }
                    }

                    // Transaction card group
                    item(key = "group_$date") {
                        val delayMs = 250 + groupIndex * 60

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(500, delayMillis = delayMs)) +
                                    slideInVertically(tween(500, delayMillis = delayMs)) { 50 },
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(AppColors.Card)
                                    .border(1.dp, AppColors.CardBorder, RoundedCornerShape(18.dp)),
                            ) {
                                txns.forEachIndexed { index, txn ->
                                    SwipeableTransactionRow(
                                        transaction = txn,
                                        showDivider = index < txns.lastIndex,
                                        onEdit = { onEditClick(txn) },
                                        onDelete = { onDeleteClick(txn) },
                                        onClick = { onTransactionClick(txn) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
