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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.components.SummaryCard
import com.naveenapps.expensemanager.core.designsystem.components.WidgetHeader
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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
    val state by viewModel.state.collectAsState()

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
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.accounts),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(AccountListAction.ClosePage)
                },
                actions = {
                    if (state.showReOrder) {
                        IconButton(
                            onClick = {
                                onAction.invoke(AccountListAction.OpenReOrder)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = stringResource(R.string.accounts_re_order),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,

                modifier = Modifier.testTag("Create"),
                onClick = { onAction.invoke(AccountListAction.CreateAccount) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_account))
                },
            )
        },
    ) { innerPadding ->
        AccountListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
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
) {
    Box(modifier = modifier) {
        if (state.accounts.isEmpty()) {
            EmptyItem(
                emptyItemText = stringResource(id = R.string.no_account_available),
                icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                item(key = "account_summary") {
                    AccountSummaryView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
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
                        shape = AppCardViewDefaults.cardShape(index, state.accounts),
                        trailingContent = {
                            AccountItemDefaults.ChevronTrailing()
                        },
                    )
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
    val incomeColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500)
    val expenseColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.red_500)

    AppCardView(
        modifier = modifier.testTag("AccountSummary"),
        cornerSize = 16.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            WidgetHeader(
                title = stringResource(R.string.account_summary),
                subTitle = pluralStringResource(
                    id = R.plurals.account_count,
                    count = accountCount,
                    accountCount,
                ),
            )

            Spacer(Modifier.height(16.dp))

            // ── Assets & Liabilities — side by side ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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

            Spacer(Modifier.height(14.dp))

            // ── Total balance row ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest
                            .copy(alpha = 0.5f),
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.total_balance),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.6f),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = totalAmount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = totalAmountTextColor?.let { colorResource(id = it) }
                        ?: MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

    AppCardView(
        modifier = modifier.width(170.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onItemClick.invoke()
                }
                .padding(14.dp),
        ) {
            // ── Icon in tinted container + name ────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(if (customImagePath != null) CircleShape else RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest
                                .copy(alpha = 0.6f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    IconOrCustomImage(
                        icon = icon,
                        customImagePath = customImagePath,
                        contentDescription = name,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (customImagePath != null) {
                            Modifier.size(30.dp)
                        } else {
                            Modifier.size(16.dp)
                        },
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Amount ─────────────────────────────────────────────
            Text(
                text = amount,
                color = amountTextColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(4.dp))

            // ── Credit limit / placeholder ─────────────────────────
            // Always rendered so all cards keep the same height
            // in a horizontal scroll row.
            Text(
                text = if (hasCreditLimit) {
                    stringResource(
                        id = R.string.available_limit,
                        availableCreditLimit,
                    )
                } else {
                    " "
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = 0.55f),
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
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
                .padding(16.dp),
            name = "Utilities is having a lengthy one",
            icon = "credit_card",
            amount = "100.00$",
            availableCreditLimit = "Available Limit 100.00$",
            amountTextColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500),
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
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
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
