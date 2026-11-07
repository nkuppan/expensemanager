package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.AppShareLink
import com.naveenapps.expensemanager.core.common.utils.shareAppMessage
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidgetCompact
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.DashboardSection
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.feature.account.list.DashBoardAccountItem
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel
import com.naveenapps.expensemanager.feature.category.list.getCategoryData
import com.naveenapps.expensemanager.feature.filter.FilterView
import com.naveenapps.expensemanager.feature.transaction.list.getTransactionItem
import kotlin.random.Random
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardScaffoldContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun DashboardScaffoldContent(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(id = R.string.home),
                actions = {
                    // Notification centre (release notes, insights), with a dot while anything is unread.
                    IconButton(onClick = { onAction.invoke(DashboardAction.OpenNotifications) }) {
                        BadgedBox(
                            badge = {
                                if (state.hasUnreadNotifications) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = stringResource(id = R.string.notification_centre_title),
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            onAction.invoke(DashboardAction.OpenSettings)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        DashboardScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 78.dp),
    ) {
        item {
            FilterView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
            )
        }
        state.backupWarning?.let { warning ->
            item("backup_warning") {
                AutoBackupWarningCard(
                    error = warning,
                    onFix = { onAction(DashboardAction.FixAutoBackup) },
                    onLater = { onAction(DashboardAction.SnoozeAutoBackupWarning) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        item {
            GettingStartedSection(state = state, onAction = onAction)
        }
        // The user's order from Customise dashboard (DashboardSection.DEFAULT_ORDER until changed).
        state.sectionOrder.forEach { section ->
            when (section) {
                DashboardSection.SUMMARY -> summarySection(state)
                DashboardSection.ACCOUNTS -> accountsSection(state, onAction)
                DashboardSection.CATEGORIES -> categoriesSection(state, onAction)
                DashboardSection.BUDGETS -> budgetsSection(state, onAction)
                DashboardSection.TRANSACTIONS -> transactionsSection(state, onAction)
                DashboardSection.UPCOMING -> upcomingSection(state, onAction)
            }
        }

        if (state.showShareBanner(System.currentTimeMillis())) {
            item("share_banner") {
                val context = LocalContext.current
                LaunchedEffect(Unit) { onAction(DashboardAction.ShareBannerShown) }
                HomeShareBanner(
                    onShare = {
                        val link = AppShareLink.playStore(AppShareLink.MEDIUM_HOME_BANNER)
                        val shared = context.shareAppMessage(
                            message = context.getString(R.string.home_share_text, link),
                            chooserTitle = context.getString(R.string.home_share_button),
                        )
                        if (shared) onAction(DashboardAction.ShareAppFromBanner)
                    },
                    onDismiss = { onAction(DashboardAction.DismissShareBanner) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
            }
        }

        item("customise_dashboard") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = { onAction(DashboardAction.OpenCustomiseDashboard) }) {
                    Icon(
                        imageVector = Icons.Outlined.DashboardCustomize,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.customise_dashboard))
                }
            }
        }
    }
}

/** Income, expense and balance for the selected period. */
private fun LazyListScope.summarySection(state: DashboardState) {
    item("summary") {
        IncomeExpenseBalanceView(
            transactionPeriod = state.transactionPeriod,
            expenseFlowState = state.expenseFlowState,
            isCompact = state.isCompactSummary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )
    }
}

/** Accounts with their balances, in a horizontal row. */
private fun LazyListScope.accountsSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    item("account_item") {
        Column {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenAccountList)
                },
            )
            if (state.accounts.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.accounts, key = { it.id }) {
                        DashBoardAccountItem(
                            modifier = Modifier
                                .wrapContentWidth(),
                            name = it.name,
                            icon = it.storedIcon.name,
                            customImagePath = it.storedIcon.customImagePath,
                            amount = it.amount.amountString ?: "",
                            availableCreditLimit = it.availableCreditLimit?.amountString
                                ?: "",
                            amountTextColor = colorResource(id = it.amountTextColor),
                            onItemClick = {
                                onAction.invoke(DashboardAction.OpenAccountEdit(it))
                            },
                        )
                    }
                }
            } else {
                EmptyItem(
                    emptyItemText = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.no_account_available_short),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

/** Spending or income by category (the donut card). */
private fun LazyListScope.categoriesSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    item("category_item") {
        Column {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                title = stringResource(id = R.string.categories),
            )
            HomeCategoryCard(
                expense = state.categoryTransactionState,
                income = state.incomeCategoryTransactionState,
                onAddTransaction = { onAction.invoke(DashboardAction.OpenTransactionEdit(null)) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/** Budgets for the current period. */
private fun LazyListScope.budgetsSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    item("budget_item") {
        Column {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                title = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.active_budgets),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenBudgetList)
                },
            )
            HomeBudgetCard(
                budgets = state.homeBudgets,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/** The latest transactions in the selected period. */
private fun LazyListScope.transactionsSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    item {
        DashboardWidgetTitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp),
            title = stringResource(id = R.string.transaction),
            onViewAllClick = {
                onAction.invoke(DashboardAction.OpenTransactionList)
            },
        )
    }
    if (state.transactions.isNotEmpty()) {
        itemsIndexed(
            items = state.transactions,
            key = { _, item -> item.id },
        ) { index, transaction ->
            AppCardView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = AppCardViewDefaults.cardShape(index, state.transactions),
            ) {
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction.invoke(DashboardAction.OpenTransactionEdit(transaction))
                        },
                    categoryName = transaction.categoryTitleResId?.let { stringResource(it) }
                        ?: transaction.categoryName,
                    categoryColor = transaction.categoryIcon.backgroundColor,
                    categoryIcon = transaction.categoryIcon.name,
                    amount = transaction.amount,
                    date = transaction.date,
                    notes = transaction.notes,
                    transactionType = transaction.transactionType,
                    isRecurring = transaction.isRecurring,
                    fromAccountName = transaction.fromAccountName,
                    fromAccountIcon = transaction.fromAccountIcon.name,
                    fromAccountColor = transaction.fromAccountIcon.backgroundColor,
                    toAccountName = transaction.toAccountName,
                    toAccountIcon = transaction.toAccountIcon?.name,
                    toAccountColor = transaction.toAccountIcon?.backgroundColor,
                )
            }
        }
    } else {
        item {
            EmptyItem(
                emptyItemText = stringResource(id = R.string.no_transactions_available),
                icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction,
                modifier = Modifier
                    .fillMaxSize()
                    .height(320.dp)
                    .padding(horizontal = 16.dp),
            )
        }
    }
}

/** Planned recurring entries (the Upcoming card). */
private fun LazyListScope.upcomingSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    item("upcoming_item") {
        Column {
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                title = stringResource(id = R.string.upcoming),
                onViewAllClick = if (state.upcoming.items.isEmpty()) {
                    null
                } else {
                    { onAction.invoke(DashboardAction.OpenRecurringList) }
                },
            )
            HomeUpcomingCard(
                upcoming = state.upcoming,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
fun IncomeExpenseBalanceView(
    expenseFlowState: ExpenseFlowState,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    if (isCompact) {
        AmountInfoWidgetCompact(
            modifier = modifier,
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
        )
    } else {
        AmountInfoWidget(
            modifier = modifier,
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun DashboardScaffoldContentPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashboardScaffoldContent(
            state = DashboardState(
                expenseFlowState = ExpenseFlowState(),
                accounts = getRandomAccountUiModel(5),
                categoryTransactionState = CategoryTransactionState(
                    pieChartData = listOf(
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                        getDummyPieChartData("", 25.0f),
                    ),
                    totalAmount = Amount(0.0, "Expenses"),
                    categoryTransactions = buildList {
                        repeat(15) {
                            add(
                                CategoryTransaction(
                                    category = getCategoryData(
                                        it,
                                        CategoryType.EXPENSE,
                                    ),
                                    amount = Amount(0.0, "100.00$"),
                                    percent = Random(100).nextFloat(),
                                    transaction = emptyList(),
                                ),
                            )
                        }
                    }.take(5),
                    categoryType = CategoryType.EXPENSE,
                ),
                transactions = listOf(
                    getTransactionItem("1"),
                    getTransactionItem("2"),
                    getTransactionItem("3"),
                ),
                transactionPeriod = "This month (Feb 2026)",
            ),
            onAction = {},
        )
    }
}

// Only new users get the checklist; long-time users (and anyone who closed it) never see it.
private const val GETTING_STARTED_MAX_TRANSACTIONS = 10

/**
 * Shown on Home while automatic backups are failing, so a broken backup never goes unnoticed.
 * "Fix" opens Automatic backup; "Later" hides it for a few days. It goes away by itself once a
 * backup succeeds or a new folder is chosen.
 */
@Composable
private fun AutoBackupWarningCard(
    error: AutoBackupError,
    onFix: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.auto_backup_warning_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            id = when (error) {
                                AutoBackupError.FOLDER_UNAVAILABLE -> R.string.auto_backup_warning_folder
                                AutoBackupError.WRITE_FAILED -> R.string.auto_backup_warning_write
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onLater) {
                    Text(
                        text = stringResource(id = R.string.auto_backup_warning_later),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                TextButton(onClick = onFix) {
                    Text(
                        text = stringResource(id = R.string.auto_backup_warning_fix),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// Display order of the "Get started" checklist. Currency and accounts aren't steps: both are
// set during onboarding (accounts come preloaded), so they'd always show as done.
private enum class SetupStep { FirstExpense, Budget }

@Composable
private fun GettingStartedSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    val done = mapOf(
        SetupStep.FirstExpense to state.hasCreatedTransaction,
        SetupStep.Budget to state.hasCurrentMonthBudget,
    )
    val visible = !state.isGettingStartedDismissed &&
        state.transactionCount < GETTING_STARTED_MAX_TRANSACTIONS &&
        done.values.any { !it }
    if (!visible) return

    GettingStartedCard(
        modifier = Modifier.fillMaxWidth(),
        done = done,
        onStepClick = { step ->
            when (step) {
                SetupStep.FirstExpense -> onAction(DashboardAction.OpenTransactionEdit(null))
                SetupStep.Budget -> onAction(DashboardAction.OpenBudgetCreate())
            }
        },
        onDismiss = { onAction(DashboardAction.DismissGettingStarted) },
    )
}

/**
 * Minimal "Get started" checklist on Home for new users: one card, like the other Home cards,
 * with a one-line header (title, progress, close) and one row per step still to do. Finished
 * steps just count towards "1 of 2". The next step has a small button, so there's always one
 * obvious thing to do. Everything inside uses the same 16dp inset as the other cards' rows, so
 * the title and the step names line up. Disappears once every step is done.
 */
@Composable
private fun GettingStartedCard(
    done: Map<SetupStep, Boolean>,
    onStepClick: (SetupStep) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = SetupStep.entries
    val completed = steps.count { done[it] == true }
    val remaining = steps.filter { done[it] != true }
    val nextStep = remaining.firstOrNull()

    AppCardView(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        cornerSize = 16.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.getting_started_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.getting_started_progress, completed, steps.size),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.getting_started_hide),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        remaining.forEach { step ->
            SetupStepRow(
                number = steps.indexOf(step) + 1,
                step = step,
                isNext = step == nextStep,
                onClick = { onStepClick(step) },
            )
        }
    }
}

/** One step still to do, on a single line: number, title, and a small button on the next one. */
@Composable
private fun SetupStepRow(
    number: Int,
    step: SetupStep,
    isNext: Boolean,
    onClick: () -> Unit,
) {
    val (title, action) = when (step) {
        SetupStep.FirstExpense -> R.string.step_expense_title to R.string.first_expense_action
        SetupStep.Budget -> R.string.step_budget_title to R.string.step_budget_action
    }

    // Same insets and text style as the rows of the other Home cards (e.g. Upcoming).
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepBadge(number = number, isDone = false, isNext = isNext)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isNext) {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(id = action),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Green check when done, a filled number for the next step, an outlined number otherwise. */
@Composable
private fun StepBadge(number: Int, isDone: Boolean, isNext: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.size(22.dp),
        shape = CircleShape,
        color = if (isDone || isNext) primary else Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = if (isDone || isNext) null else BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun GettingStartedCardPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        GettingStartedCard(
            done = mapOf(
                SetupStep.FirstExpense to false,
                SetupStep.Budget to false,
            ),
            onStepClick = {},
            onDismiss = {},
        )
    }
}
