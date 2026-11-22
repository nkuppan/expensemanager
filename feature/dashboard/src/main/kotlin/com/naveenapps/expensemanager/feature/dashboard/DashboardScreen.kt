package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.AppShareLink
import com.naveenapps.expensemanager.core.common.utils.shareAppMessage
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidgetCompact
import com.naveenapps.expensemanager.core.designsystem.em.EmBadge
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.ui.utils.categoryDisplayName
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
import com.naveenapps.expensemanager.feature.debt.list.DebtTotalsCard
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
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(id = R.string.home),
                actions = {
                    // Notification centre (release notes, insights), with a dot while anything is unread.
                    EmIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = stringResource(id = R.string.notification_centre_title),
                        onClick = { onAction.invoke(DashboardAction.OpenNotifications) },
                        badge = if (state.hasUnreadNotifications) EmBadge.Dot else null,
                    )
                    EmIconButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                        onClick = { onAction.invoke(DashboardAction.OpenSettings) },
                    )
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
                    .padding(end = EmTheme.spacing.sm),
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
                        .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
                )
            }
        }
        item("reminder_banner") {
            // Draws nothing unless reminders are blocked or off (and the user hasn't snoozed it).
            ReminderBannerItem(
                state = state,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
            )
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
                DashboardSection.DEBTS -> debtsSection(state, onAction)
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
                    modifier = Modifier.padding(
                        start = EmTheme.spacing.lg,
                        end = EmTheme.spacing.lg,
                        top = SectionSpacing,
                    ),
                )
            }
        }

        item("customise_dashboard") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = EmTheme.spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmButton(
                    text = stringResource(id = R.string.customise_dashboard),
                    onClick = { onAction(DashboardAction.OpenCustomiseDashboard) },
                    variant = EmButtonVariant.Tertiary,
                    leadingIcon = Icons.Outlined.DashboardCustomize,
                )
            }
        }
    }
}

/**
 * Gap above every reorderable Home section. Each section owns exactly this space above itself
 * and nothing below, so the spacing is the same whatever order the user picks.
 */
private val SectionSpacing = 12.dp

/**
 * A section's title row: [SectionSpacing] above and 4dp before the section's content. The header
 * itself has one fixed height with or without "View all", so every section lines up.
 */
@Composable
private fun Modifier.sectionTitle(): Modifier = this
    .fillMaxWidth()
    .padding(top = SectionSpacing, bottom = EmTheme.spacing.xs)
    .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.xs)

/** Income, expense and balance for the selected period. */
private fun LazyListScope.summarySection(state: DashboardState) {
    item("summary") {
        IncomeExpenseBalanceView(
            transactionPeriod = state.transactionPeriod,
            expenseFlowState = state.expenseFlowState,
            isCompact = state.isCompactSummary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.lg, top = SectionSpacing),
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
            EmSectionHeader(
                modifier = Modifier.sectionTitle(),
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
                    contentPadding = PaddingValues(horizontal = EmTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
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
                            amountTextColor = emColorOf(it.amountTone),
                            onItemClick = {
                                onAction.invoke(DashboardAction.OpenAccountEdit(it))
                            },
                        )
                    }
                }
            } else {
                EmEmptyState(
                    illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.no_account_available_short),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    compact = true,
                )
            }
        }
    }
}

/** What people owe the user and what the user owes; only while there are open debts. */
private fun LazyListScope.debtsSection(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
) {
    val debts = state.debtTotals ?: return
    item("debts_item") {
        Column {
            EmSectionHeader(
                modifier = Modifier.sectionTitle(),
                title = stringResource(id = R.string.home_debts_title),
                onViewAllClick = { onAction.invoke(DashboardAction.OpenDebts) },
            )
            DebtTotalsCard(
                toGet = debts.toGet,
                toGive = debts.toGive,
                onClick = { onAction.invoke(DashboardAction.OpenDebts) },
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
            )
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
            EmSectionHeader(
                modifier = Modifier.sectionTitle(),
                title = stringResource(id = R.string.categories),
            )
            HomeCategoryCard(
                expense = state.categoryTransactionState,
                income = state.incomeCategoryTransactionState,
                onAddTransaction = { onAction.invoke(DashboardAction.OpenTransactionEdit(null)) },
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
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
            EmSectionHeader(
                modifier = Modifier.sectionTitle(),
                title = stringResource(id = com.naveenapps.expensemanager.feature.budget.R.string.active_budgets),
                onViewAllClick = {
                    onAction.invoke(DashboardAction.OpenBudgetList)
                },
            )
            HomeBudgetCard(
                budgets = state.homeBudgets,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
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
        EmSectionHeader(
            modifier = Modifier.sectionTitle(),
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
            EmCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xxs),
                position = EmCardPosition.of(index, state.transactions),
            ) {
                TransactionItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction.invoke(DashboardAction.OpenTransactionEdit(transaction))
                        },
                    categoryName = transaction.categoryDisplayName(),
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
            EmEmptyState(
                illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_transaction),
                title = stringResource(id = R.string.no_transactions_available),
                modifier = Modifier
                    .fillMaxSize()
                    .height(320.dp)
                    .padding(horizontal = EmTheme.spacing.lg),
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
            EmSectionHeader(
                modifier = Modifier.sectionTitle(),
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
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
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

@EmPreviews
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
    EmCard(modifier = modifier, variant = EmCardVariant.Error) {
        Column(
            modifier = Modifier.padding(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.sm,
                top = EmTheme.spacing.lg,
                bottom = EmTheme.spacing.sm,
            ),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                EmIcon(
                    icon = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.padding(top = EmTheme.spacing.xxs),
                )
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = EmTheme.spacing.sm),
                ) {
                    EmText(
                        text = stringResource(id = R.string.auto_backup_warning_title),
                        role = EmTextRole.SectionTitle,
                    )
                    EmText(
                        text = stringResource(
                            id = when (error) {
                                AutoBackupError.FOLDER_UNAVAILABLE -> R.string.auto_backup_warning_folder
                                AutoBackupError.WRITE_FAILED -> R.string.auto_backup_warning_write
                            },
                        ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                EmButton(
                    text = stringResource(id = R.string.auto_backup_warning_later),
                    onClick = onLater,
                    variant = EmButtonVariant.Tertiary,
                )
                EmButton(
                    text = stringResource(id = R.string.auto_backup_warning_fix),
                    onClick = onFix,
                    variant = EmButtonVariant.Tertiary,
                )
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

    EmCard(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmText(
                text = stringResource(id = R.string.getting_started_title),
                role = EmTextRole.SectionTitle,
            )
            Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
            EmText(
                text = stringResource(id = R.string.getting_started_progress, completed, steps.size),
                modifier = Modifier.weight(1f),
                role = EmTextRole.Label,
                color = EmTextColor.Primary,
            )
            EmIconButton(
                icon = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.getting_started_hide),
                onClick = onDismiss,
                tint = EmTheme.colors.scheme.onSurfaceVariant,
            )
        }
        EmDivider()
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
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm)
            .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepBadge(number = number, isDone = false, isNext = isNext)
        Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        EmText(
            text = stringResource(id = title),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isNext) {
            EmButton(
                text = stringResource(id = action),
                onClick = onClick,
                modifier = Modifier.height(30.dp),
            )
        } else {
            EmIcon(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                size = EmTheme.icons.md,
                tint = EmTheme.colors.scheme.onSurfaceVariant,
            )
        }
    }
}

/** Green check when done, a filled number for the next step, an outlined number otherwise. */
@Composable
private fun StepBadge(number: Int, isDone: Boolean, isNext: Boolean) {
    val primary = EmTheme.colors.scheme.primary
    val filled = isDone || isNext
    Box(
        modifier = Modifier
            .size(EmTheme.icons.lg)
            .clip(EmTheme.shapes.pill)
            .background(if (filled) primary else Color.Transparent)
            .then(
                if (filled) Modifier else Modifier.border(1.5.dp, EmTheme.colors.scheme.outline, EmTheme.shapes.pill),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDone) {
                EmIcon(
                    icon = Icons.Default.Check,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onPrimary,
                )
            } else {
                EmText(
                    text = number.toString(),
                    role = EmTextRole.Label,
                    color = if (isNext) {
                        EmTheme.colors.scheme.onPrimary
                    } else {
                        EmTheme.colors.scheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@EmPreviews
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
