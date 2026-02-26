package com.naveenapps.expensemanager.feature.account.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date
import java.util.Random

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
                item {
                    // Hint banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.account_count,
                                count = state.accounts.size,
                                state.accounts.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashBoardAccountItem(
    name: String,
    icon: String,
    amount: String,
    availableCreditLimit: String?,
    amountTextColor: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasCreditLimit = !availableCreditLimit.isNullOrBlank()

    AppCardView(
        modifier = modifier.width(170.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            // ── Icon in tinted container + name ────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest
                                .copy(alpha = 0.6f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = context.getDrawable(icon)),
                        contentDescription = name,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
): Account {
    return Account(
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
}

fun getRandomAccountData(totalCount: Int = 10): List<Account> {
    return buildList {
        val random = Random()
        repeat(totalCount) { index ->
            val isEven = random.nextInt() % 2 == 0
            add(
                getAccountData(
                    index = index,
                    accountType = if (isEven) AccountType.CREDIT else AccountType.REGULAR,
                    amount = 100.0,
                    creditLimit = if (isEven) 2000.0 else 0.0
                ),
            )
        }
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AccountListContentView(
            state = AccountListState(
                accounts = getRandomAccountUiModel(10),
                showReOrder = true,
            ),
            onAction = {},
        )
    }
}
