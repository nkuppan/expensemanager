package com.naveenapps.expensemanager.feature.account.list

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.components.LoadingItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import java.util.Date
import java.util.Random

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel = hiltViewModel(),
) {
    val collectAsState by viewModel.accounts.collectAsState()

    AccountListContentView(
        collectAsState,
        closePage = viewModel::closePage,
        openCreateScreen = viewModel::openCreateScreen
    )
}

@Composable
internal fun AccountListContentView(
    accountUiState: UiState<List<AccountUiModel>>,
    closePage: () -> Unit,
    openCreateScreen: (AccountUiModel?) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                title = stringResource(R.string.accounts),
                onClick = closePage,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.testTag("Create"),
                onClick = {
                    openCreateScreen(null)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        AccountListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            accountUiState = accountUiState,
            onItemClick = openCreateScreen,
        )
    }
}

@Composable
private fun AccountListScreenContent(
    accountUiState: UiState<List<AccountUiModel>>,
    onItemClick: ((AccountUiModel) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (accountUiState) {
            UiState.Empty -> {
                EmptyItem(
                    emptyItemText = stringResource(id = R.string.no_account_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_accounts,
                    modifier = Modifier.fillMaxSize()
                )
            }

            UiState.Loading -> {
                LoadingItem()
            }

            is UiState.Success -> {
                LazyColumn {
                    items(accountUiState.data, key = { it.id }) { account ->
                        AccountItem(
                            modifier = Modifier
                                .clickable {
                                    onItemClick?.invoke(account)
                                }
                                .then(ItemSpecModifier)
                                .testTag("Item"),
                            name = account.name,
                            icon = account.storedIcon.name,
                            iconBackgroundColor = account.storedIcon.backgroundColor,
                            amount = account.amount.amountString,
                            amountTextColor = account.amountTextColor,
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun AccountItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String?,
    amountTextColor: Int?,
    modifier: Modifier = Modifier,
    endIcon: ImageVector? = null,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (amount != null && amountTextColor != null) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(id = amountTextColor),
            )
        }
        if (endIcon != null) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp),
                imageVector = endIcon,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun AccountCheckedItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            checked = isSelected,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun DashBoardAccountItem(
    name: String,
    icon: String,
    modifier: Modifier = Modifier,
    amount: String,
    amountTextColor: Color,
    backgroundColor: Color,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(160.dp),
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    painter = painterResource(id = context.getDrawable(icon)),
                    contentDescription = name,
                )
            }
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 16.dp),
                color = amountTextColor,
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

fun getAccountData(
    index: Int,
    accountType: AccountType,
    amount: Double,
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
        createdOn = Date(),
        updatedOn = Date(),
    )
}

fun getRandomAccountData(totalCount: Int = 10): List<Account> {
    return buildList {
        val random = Random()

        repeat(totalCount) {
            val isEven = random.nextInt() % 2 == 0
            add(
                getAccountData(
                    it,
                    if (isEven) {
                        AccountType.CREDIT
                    } else {
                        AccountType.REGULAR
                    },
                    amount = 100.0,
                ),
            )
        }
    }
}

fun getRandomAccountUiModel(count: Int) = getRandomAccountData(count).map {
    it.toAccountUiModel(Amount(it.amount, "${it.amount}$"))
}

@Preview
@Composable
private fun DashBoardAccountItemPreview() {
    ExpenseManagerTheme {
        DashBoardAccountItem(
            modifier = Modifier
                .wrapContentWidth()
                .padding(16.dp),
            name = "Utilities is having a lengthy one",
            icon = "credit_card",
            amount = "100.00$",
            amountTextColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500),
            backgroundColor = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.black_100),
        )
    }
}

@Preview
@Composable
private fun AccountItemPreview() {
    ExpenseManagerTheme {
        AccountItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            name = "Utilities",
            icon = "credit_card",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
        )
    }
}

@Preview
@Composable
private fun AccountCheckedItemPreview() {
    ExpenseManagerTheme {
        AccountCheckedItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            name = "First Account",
            icon = "savings",
            iconBackgroundColor = "#000000",
            isSelected = true,
        )
    }
}

@Preview
@Composable
private fun AccountListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        AccountListContentView(
            accountUiState = UiState.Loading,
            openCreateScreen = { },
            closePage = { },
        )
    }
}

@Preview
@Composable
private fun AccountListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        AccountListContentView(
            accountUiState = UiState.Empty,
            openCreateScreen = { },
            closePage = { },
        )
    }
}

@Preview
@Composable
private fun AccountListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        AccountListContentView(
            accountUiState = UiState.Success(
                getRandomAccountUiModel(10),
            ),
            openCreateScreen = { },
            closePage = { },
        )
    }
}
