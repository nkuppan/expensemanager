package com.naveenapps.expensemanager.feature.account.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.components.SummaryCard
import com.naveenapps.expensemanager.core.designsystem.components.WidgetHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconOrCustomImage
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import java.util.Date
import java.util.Random
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AccountListContentView(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
internal fun AccountListContentView(
    state: AccountListState,
    onAction: (AccountListAction) -> Unit,
) {
    val snackbarHostState = remember { EmSnackbarHostState() }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                title = stringResource(R.string.accounts),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(AccountListAction.ClosePage)
                },
                actions = {
                    if (state.showReOrder) {
                        EmIconButton(
                            icon = Icons.Rounded.SwapVert,
                            contentDescription = stringResource(R.string.accounts_re_order),
                            onClick = {
                                onAction.invoke(AccountListAction.OpenReOrder)
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            EmFab(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_account),
                onClick = { onAction.invoke(AccountListAction.CreateAccount) },
                modifier = Modifier.testTag("Create"),
                label = stringResource(R.string.add_account),
            )
        },
    ) { innerPadding ->
        AccountListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
            onUnarchive = { onAction.invoke(AccountListAction.Unarchive(it)) },
            onItemClick = {
                onAction.invoke(AccountListAction.EditAccount(it))
            },
        )
    }
}

@Composable
private fun AccountListScreenContent(
    state: AccountListState,
    onItemClick: ((AccountUiModel) -> Unit)?,
    modifier: Modifier = Modifier,
    onUnarchive: (AccountUiModel) -> Unit = {},
) {
    Box(modifier = modifier) {
        if (state.accounts.isEmpty() && state.archivedAccounts.isEmpty()) {
            EmEmptyState(
                illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts),
                title = stringResource(id = R.string.no_account_available),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = EmTheme.spacing.lg,
                    end = EmTheme.spacing.lg,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
            ) {
                item(key = "account_summary") {
                    AccountSummaryView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = EmTheme.spacing.sm, bottom = EmTheme.spacing.lg),
                        accountCount = state.accounts.size,
                        assetsAmount = state.assetsAmount?.amountString.orEmpty(),
                        liabilitiesAmount = state.liabilitiesAmount?.amountString.orEmpty(),
                        totalAmount = state.totalAmount?.amountString.orEmpty(),
                        totalAmountTextColor = state.totalAmountTextColor,
                    )
                }

                itemsIndexed(
                    items = state.accounts,
                    key = { _, item -> item.id },
                ) { index, account ->
                    AccountItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("Item")
                            .animateItem(),
                        onClick = { onItemClick?.invoke(account) },
                        name = account.name,
                        icon = account.storedIcon.name,
                        iconBackgroundColor = account.storedIcon.backgroundColor,
                        amount = account.amount.amountString,
                        subtitle = account.availableCreditLimit?.amountString,
                        amountTextColor = account.amountTextColor,
                        customImagePath = account.storedIcon.customImagePath,
                        position = EmCardPosition.of(index, state.accounts),
                        trailingContent = {
                            AccountItemDefaults.ChevronTrailing()
                        },
                    )
                }

                if (state.archivedAccounts.isNotEmpty()) {
                    item("archived_header") {
                        EmSectionHeader(
                            title = stringResource(id = R.string.archived_accounts),
                            modifier = Modifier.padding(top = EmTheme.spacing.xl),
                        )
                    }
                    itemsIndexed(
                        items = state.archivedAccounts,
                        key = { _, item -> "archived_${item.id}" },
                    ) { index, account ->
                        AccountItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            onClick = null,
                            name = account.name,
                            icon = account.storedIcon.name,
                            iconBackgroundColor = account.storedIcon.backgroundColor,
                            amount = account.amount.amountString,
                            amountTextColor = account.amountTextColor,
                            customImagePath = account.storedIcon.customImagePath,
                            position = EmCardPosition.of(index, state.archivedAccounts),
                            trailingContent = {
                                EmButton(
                                    text = stringResource(id = R.string.unarchive),
                                    onClick = { onUnarchive(account) },
                                    variant = EmButtonVariant.Tertiary,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Summary of every account, styled like the dashboard's transaction summary
 * (header · subtitle, tinted tiles, muted total row).
 */
@Composable
private fun AccountSummaryView(
    accountCount: Int,
    assetsAmount: String,
    liabilitiesAmount: String,
    totalAmount: String,
    totalAmountTextColor: Int?,
    modifier: Modifier = Modifier,
) {
    val incomeColor = EmTheme.colors.income
    val expenseColor = EmTheme.colors.expense

    EmCard(
        modifier = modifier.testTag("AccountSummary"),
    ) {
        Column(modifier = Modifier.padding(EmTheme.spacing.lg)) {
            WidgetHeader(
                title = stringResource(R.string.account_summary),
                subTitle = pluralStringResource(
                    id = R.plurals.account_count,
                    count = accountCount,
                    accountCount,
                ),
            )

            Spacer(Modifier.height(EmTheme.spacing.lg))

            // ── Assets & Liabilities — side by side ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
            ) {
                SummaryCard(
                    label = stringResource(R.string.assets),
                    amount = assetsAmount,
                    icon = Icons.Rounded.AccountBalance,
                    tintColor = incomeColor,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = stringResource(R.string.liabilities),
                    amount = liabilitiesAmount,
                    icon = Icons.Rounded.CreditCard,
                    tintColor = expenseColor,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(EmTheme.spacing.md))

            // ── Total balance row ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(EmTheme.shapes.sm)
                    .background(
                        EmTheme.colors.scheme.surfaceContainerHighest,
                    )
                    .padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = stringResource(R.string.total_balance),
                    color = EmTheme.colors.scheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                EmAmountText(
                    amount = totalAmount,
                    color = totalAmountTextColor?.let { emColorOf(it) }
                        ?: EmTheme.colors.scheme.onSurface,
                    size = EmAmountSize.Large,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashBoardAccountItem(
    name: String,
    icon: String,
    amount: String,
    availableCreditLimit: String?,
    amountTextColor: Color,
    modifier: Modifier = Modifier,
    customImagePath: String? = null,
    onItemClick: (() -> Unit) = {},
) {
    val hasCreditLimit = !availableCreditLimit.isNullOrBlank()

    EmCard(
        modifier = modifier.width(170.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onItemClick.invoke()
                }
                .padding(EmTheme.spacing.md),
        ) {
            // ── Icon in tinted container + name ────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(if (customImagePath != null) EmTheme.shapes.pill else EmTheme.shapes.sm)
                        .background(
                            EmTheme.colors.scheme.surfaceContainerHighest,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    IconOrCustomImage(
                        icon = icon,
                        customImagePath = customImagePath,
                        contentDescription = name,
                        tint = EmTheme.colors.scheme.onSurfaceVariant,
                        modifier = if (customImagePath != null) {
                            Modifier.size(30.dp)
                        } else {
                            Modifier.size(16.dp)
                        },
                    )
                }
                Spacer(Modifier.width(EmTheme.spacing.sm))
                EmText(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(EmTheme.spacing.md))

            // ── Amount ─────────────────────────────────────────────
            EmAmountText(
                amount = amount,
                color = amountTextColor,
                size = EmAmountSize.Small,
            )

            Spacer(Modifier.height(EmTheme.spacing.xs))

            // ── Credit limit / placeholder ─────────────────────────
            // Always rendered so all cards keep the same height
            // in a horizontal scroll row.
            EmText(
                text = if (hasCreditLimit) {
                    stringResource(
                        id = R.string.available_limit,
                        availableCreditLimit,
                    )
                } else {
                    " "
                },
                modifier = Modifier.basicMarquee(),
                role = EmTextRole.Supporting,
                color = EmTheme.colors.scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

// ─── Preview helpers ────────────────────────────────────────────────────────

fun getAccountData(
    index: Int,
    accountType: AccountType,
    amount: Double,
    creditLimit: Double,
): Account = Account(
    id = "$index",
    name = "Account $index",
    type = accountType,
    storedIcon = StoredIcon(
        name = "credit_card",
        backgroundColor = "#000000",
    ),
    amount = amount,
    creditLimit = creditLimit,
    createdOn = Date(),
    updatedOn = Date(),
    currencyCode = "USD",
)

fun getRandomAccountData(totalCount: Int = 10): List<Account> = buildList {
    val random = Random()
    repeat(totalCount) { index ->
        val isEven = random.nextInt() % 2 == 0
        add(
            getAccountData(
                index = index,
                accountType = if (isEven) AccountType.CREDIT else AccountType.REGULAR,
                amount = 100.0,
                creditLimit = if (isEven) 2000.0 else 0.0,
            ),
        )
    }
}

fun getRandomAccountUiModel(count: Int) = getRandomAccountData(count).map {
    it.toAccountUiModel(
        amount = Amount(amount = it.amount, amountString = "${it.amount}$"),
        availableCreditLimit = Amount(amount = it.creditLimit, amountString = "${it.creditLimit}$"),
    )
}

@Preview
@Composable
private fun DashBoardAccountItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashBoardAccountItem(
            modifier = Modifier
                .wrapContentWidth()
                .padding(EmTheme.spacing.lg),
            name = "Utilities is having a lengthy one",
            icon = "credit_card",
            amount = "100.00$",
            availableCreditLimit = "Available Limit 100.00$",
            amountTextColor = EmTheme.colors.income,
        )
    }
}

@Preview
@Composable
private fun AccountItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AccountItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = EmTheme.spacing.lg,
                    end = EmTheme.spacing.lg,
                    top = EmTheme.spacing.md,
                    bottom = EmTheme.spacing.md,
                ),
            name = "Utilities",
            icon = "credit_card",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            subtitle = "Available limit ₹ 5,14,000.00",
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
        )
    }
}

@Preview
@Composable
private fun AccountListItemEmptyStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AccountListContentView(
            state = AccountListState(
                accounts = emptyList(),
                showReOrder = true,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun AccountListItemSuccessStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AccountListContentView(
            state = AccountListState(
                accounts = getRandomAccountUiModel(10),
                showReOrder = true,
                assetsAmount = Amount(amount = 1500.0, amountString = "1500.0$"),
                liabilitiesAmount = Amount(amount = 500.0, amountString = "500.0$"),
                totalAmount = Amount(amount = 1000.0, amountString = "1000.0$"),
                totalAmountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
            ),
            onAction = {},
        )
    }
}
